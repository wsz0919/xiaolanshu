package com.wsz.xiaolanshu.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.comment.biz.constant.MQConstants;
import com.wsz.xiaolanshu.comment.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentDO;
import com.wsz.xiaolanshu.comment.biz.enums.CommentLevelEnum;
import com.wsz.xiaolanshu.comment.biz.mapper.CommentDOMapper;
import com.wsz.xiaolanshu.comment.biz.mapper.NoteCountDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 16:07
 * @Company:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_DELETE_COMMENT, // Group
        topic = MQConstants.TOPIC_DELETE_COMMENT // 消费的主题 Topic
)
public class DeleteCommentConsumer implements RocketMQListener<String> {

    // 每秒创建 1000 个令牌
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Resource
    private CommentDOMapper commentDOMapper;

    @Resource
    private NoteCountDOMapper noteCountDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String body) {
        // 令牌桶流控
        rateLimiter.acquire();

        log.info("## 【删除评论 - 后续业务处理】消费者消费成功, body: {}", body);

        CommentDO commentDO = JsonUtils.parseObject(body, CommentDO.class);

        // 评论级别
        Integer level = commentDO.getLevel();

        CommentLevelEnum commentLevelEnum = CommentLevelEnum.valueOf(level);

        switch (commentLevelEnum) {
            case ONE -> // 一级评论
                    handleOneLevelComment(commentDO);
            case TWO -> // 二级评论
                    handleTwoLevelComment(commentDO);
        }
    }

    /**
     * 一级评论处理
     * @param commentDO
     */
    private void handleOneLevelComment(CommentDO commentDO) {
        Long commentId = commentDO.getId();
        Long noteId = commentDO.getNoteId();

        // 1. 关联评论删除（一级评论下所有子评论，都需要删除）
        int count = commentDOMapper.deleteByParentId(commentId);

        // 2. 计数更新（笔记下总评论数）
        // 更新 Redis 缓存
        String redisKey = RedisConstants.buildNoteCommentTotalKey(noteId);
        boolean hasKey = redisTemplate.hasKey(redisKey);

        if (hasKey) {
            // 笔记评论总数 -1
            redisTemplate.opsForHash().increment(redisKey, RedisConstants.FIELD_COMMENT_TOTAL, -(count + 1));
        }

        // 更新 t_note_count 计数表
        noteCountDOMapper.updateCommentTotalByNoteId(noteId, -(count + 1));
    }

    /**
     * 二级评论处理
     * @param commentDO
     */
    private void handleTwoLevelComment(CommentDO commentDO) {
        Long commentId = commentDO.getId();

        // 1. 批量删除关联评论（递归查询回复评论，并批量删除）
        List<Long> replyCommentIds = Lists.newArrayList();
        recurrentGetReplyCommentId(replyCommentIds, commentId);

        int count = 0;
        if (CollUtil.isNotEmpty(replyCommentIds)) {
            // 【注意】这里如果报错，请看第三步的 Mapper 修改！
            count = commentDOMapper.deleteByIds(replyCommentIds);
        }

        // 2. 更新一级评论的计数
        Long parentCommentId = commentDO.getParentId();
        String redisKey = RedisConstants.buildCountCommentKey(parentCommentId);
        if (redisTemplate.hasKey(redisKey)) {
            redisTemplate.opsForHash().increment(redisKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL, -(count + 1));
        }

        // ================== 【核心修复逻辑】 ==================
        // 3. 不管别的，强制重新计算一级评论的最早回复，防脏数据！
        CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(parentCommentId);
        Long earliestCommentId = Objects.nonNull(earliestCommentDO) ? earliestCommentDO.getId() : 0L;
        commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliestCommentId, parentCommentId);

        // 4. 清理一级评论缓存
        redisTemplate.delete(RedisConstants.buildCommentDetailKey(parentCommentId));
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, parentCommentId, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {}
            @Override
            public void onException(Throwable throwable) {}
        });

        // 5. 如果删完后该一级评论下没有任何二级评论了，必须删掉 Redis 的标记！
        if (earliestCommentId == 0L) {
            redisTemplate.delete(RedisConstants.buildHaveFirstReplyCommentKey(parentCommentId));
        }

        // 4. 重新计算一级评论的热度值
        Set<Long> commentIds = Sets.newHashSetWithExpectedSize(1);
        commentIds.add(parentCommentId);

        // 异步发送计数 MQ, 更新评论热度值
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(commentIds))
                .build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COMMENT_HEAT_UPDATE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论热度值更新】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论热度值更新】MQ 发送异常: ", throwable);
            }
        });
    }

    /**
     * 递归获取全部回复的评论 ID
     * @param commentIds
     * @param commentId
     */
    private void recurrentGetReplyCommentId(List<Long> commentIds, Long commentId) {
        // 【核心修复】必须要用返回 List 的查询，否则有多个子评论时必定抛出 TooManyResultsException 导致消费者崩溃
        List<CommentDO> replyCommentDOs = commentDOMapper.selectListByReplyCommentId(commentId);

        if (CollUtil.isEmpty(replyCommentDOs)) return;

        for (CommentDO replyCommentDO : replyCommentDOs) {
            commentIds.add(replyCommentDO.getId());
            // 递归调用
            recurrentGetReplyCommentId(commentIds, replyCommentDO.getId());
        }
    }

}

