package com.wsz.xiaolanshu.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.comment.biz.constant.MQConstants;
import com.wsz.xiaolanshu.comment.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.comment.biz.domain.bo.CommentBO;
import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentDO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.PublishCommentMqDTO;
import com.wsz.xiaolanshu.comment.biz.enums.CommentLevelEnum;
import com.wsz.xiaolanshu.comment.biz.mapper.CommentDOMapper;
import com.wsz.xiaolanshu.comment.biz.mapper.NoteCountDOMapper;
import com.wsz.xiaolanshu.comment.biz.rpc.KeyValueRpcService;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 15:08
 * @Company:
 */
@Component
@Slf4j
public class Comment2DBConsumer {

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    private DefaultMQPushConsumer consumer;

    // 每秒创建 1000 个令牌
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Resource
    private CommentDOMapper commentDOMapper;

    @Resource
    private NoteCountDOMapper noteCountDOMapper; // 新增注入：用于同步更新笔记评论总数

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private KeyValueRpcService keyValueRpcService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public DefaultMQPushConsumer mqPushConsumer() throws MQClientException {
        String group = "xiaolanshu_group_" + MQConstants.TOPIC_PUBLISH_COMMENT;
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(MQConstants.TOPIC_PUBLISH_COMMENT, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setConsumeMessageBatchMaxSize(30);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            log.info("==> 本批次消息大小: {}", msgs.size());
            try {
                rateLimiter.acquire();

                List<PublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String msgJson = new String(msg.getBody());
                    log.info("==> Consumer - Received message: {}", msgJson);
                    publishCommentMqDTOS.add(JsonUtils.parseObject(msgJson, PublishCommentMqDTO.class));
                });

                List<Long> replyCommentIds = publishCommentMqDTOS.stream()
                        .filter(dto -> Objects.nonNull(dto.getReplyCommentId()))
                        .map(PublishCommentMqDTO::getReplyCommentId).toList();

                List<CommentDO> replyCommentDOS = null;
                if (CollUtil.isNotEmpty(replyCommentIds)) {
                    replyCommentDOS = commentDOMapper.selectByCommentIds(replyCommentIds);
                }

                Map<Long, CommentDO> commentIdAndCommentDOMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(replyCommentDOS)) {
                    commentIdAndCommentDOMap = replyCommentDOS.stream().collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
                }

                List<CommentBO> commentBOS = Lists.newArrayList();
                for (PublishCommentMqDTO dto : publishCommentMqDTOS) {
                    String imageUrl = dto.getImageUrl();
                    CommentBO commentBO = CommentBO.builder()
                            .id(dto.getCommentId())
                            .noteId(dto.getNoteId())
                            .userId(dto.getCreatorId())
                            .isContentEmpty(true)
                            .imageUrl(StringUtils.isBlank(imageUrl) ? "" : imageUrl)
                            .level(CommentLevelEnum.ONE.getCode())
                            .parentId(dto.getNoteId())
                            .createTime(dto.getCreateTime())
                            .updateTime(dto.getCreateTime())
                            .isTop(false)
                            .replyTotal(0L)
                            .likeTotal(0L)
                            .replyCommentId(0L)
                            .replyUserId(0L)
                            .build();

                    String content = dto.getContent();
                    if (StringUtils.isNotBlank(content)) {
                        commentBO.setContentUuid(UUID.randomUUID().toString());
                        commentBO.setIsContentEmpty(false);
                        commentBO.setContent(content);
                    }

                    Long replyCommentId = dto.getReplyCommentId();
                    if (Objects.nonNull(replyCommentId)) {
                        CommentDO replyCommentDO = commentIdAndCommentDOMap.get(replyCommentId);
                        if (Objects.nonNull(replyCommentDO)) {
                            commentBO.setLevel(CommentLevelEnum.TWO.getCode());
                            commentBO.setReplyCommentId(dto.getReplyCommentId());
                            commentBO.setParentId(replyCommentDO.getId());
                            if (Objects.equals(replyCommentDO.getLevel(), CommentLevelEnum.TWO.getCode())) {
                                commentBO.setParentId(replyCommentDO.getParentId());
                            }
                            commentBO.setReplyUserId(replyCommentDO.getUserId());
                        }
                    }
                    commentBOS.add(commentBO);
                }

                // 统计新增评论数，准备同步更新
                Map<Long, Long> noteIdCountMap = commentBOS.stream()
                        .collect(Collectors.groupingBy(CommentBO::getNoteId, Collectors.counting()));

                Integer insertedRows = transactionTemplate.execute(status -> {
                    try {
                        Integer count = commentDOMapper.batchInsert(commentBOS);

                        List<CommentBO> commentContentNotEmptyBOS = commentBOS.stream()
                                .filter(bo -> Boolean.FALSE.equals(bo.getIsContentEmpty()))
                                .toList();
                        if (CollUtil.isNotEmpty(commentContentNotEmptyBOS)) {
                            keyValueRpcService.batchSaveCommentContent(commentContentNotEmptyBOS);
                        }

                        // ================== 【核心修复：同步更新该笔记的评论总数 DB】 ==================
                        for (Map.Entry<Long, Long> entry : noteIdCountMap.entrySet()) {
                            noteCountDOMapper.insertOrUpdateCommentTotalByNoteId(entry.getKey(), entry.getValue().intValue());
                        }

                        return count;
                    } catch (Exception ex) {
                        status.setRollbackOnly();
                        log.error("", ex);
                        throw ex;
                    }
                });

                if (Objects.nonNull(insertedRows) && insertedRows > 0) {
                    syncOneLevelComment2RedisZSet(commentBOS);
                    updateChildCommentTotal(commentBOS);
                    updateFirstReplyCommentId(commentBOS);

                    // ================== 【核心修复：同步更新 Redis 的 count:note:笔记ID】 ==================
                    for (Map.Entry<Long, Long> entry : noteIdCountMap.entrySet()) {
                        String noteCommentTotalKey = RedisConstants.buildNoteCommentTotalKey(entry.getKey());
                        if (Boolean.TRUE.equals(redisTemplate.hasKey(noteCommentTotalKey))) {
                            redisTemplate.opsForHash().increment(noteCommentTotalKey, RedisConstants.FIELD_COMMENT_TOTAL, entry.getValue().intValue());
                        }
                    }

                    deleteTwoLevelCommentRedisCache(commentBOS);
                    // 注意：这里已经彻底移除了往 TOPIC_COUNT_NOTE_COMMENT 发消息的代码，杜绝双重计数！
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });

        consumer.start();
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try {
                consumer.shutdown();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    private void updateChildCommentTotal(List<CommentBO> commentBOS) {
        Map<Long, Long> parentCountMap = commentBOS.stream()
                .filter(bo -> Objects.equals(bo.getLevel(), CommentLevelEnum.TWO.getCode()))
                .collect(Collectors.groupingBy(CommentBO::getParentId, Collectors.counting()));

        if (CollUtil.isEmpty(parentCountMap)) return;

        for (Map.Entry<Long, Long> entry : parentCountMap.entrySet()) {
            Long parentId = entry.getKey();
            int count = entry.getValue().intValue();

            commentDOMapper.updateChildCommentTotalById(parentId, count);

            String redisKey = RedisConstants.buildCountCommentKey(parentId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                redisTemplate.opsForHash().increment(redisKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL, count);
            }
        }
    }

    private void updateFirstReplyCommentId(List<CommentBO> commentBOS) {
        List<Long> parentIds = commentBOS.stream()
                .filter(commentBO -> Objects.equals(commentBO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .map(CommentBO::getParentId)
                .distinct()
                .toList();

        if (CollUtil.isEmpty(parentIds)) return;

        List<String> keys = parentIds.stream()
                .map(RedisConstants::buildHaveFirstReplyCommentKey)
                .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        List<Long> missingCommentIds = Lists.newArrayList();
        for (int i = 0; i < values.size(); i++) {
            if (Objects.isNull(values.get(i))) {
                missingCommentIds.add(parentIds.get(i));
            }
        }

        if (CollUtil.isNotEmpty(missingCommentIds)) {
            List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(missingCommentIds);
            List<CommentDO> needUpdateCommentDOS = commentDOS.stream()
                    .filter(commentDO -> commentDO.getFirstReplyCommentId() == 0)
                    .toList();

            for (CommentDO needUpdateCommentDO : needUpdateCommentDOS) {
                Long parentId = needUpdateCommentDO.getId();
                CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(parentId);
                if (Objects.nonNull(earliestCommentDO)) {
                    Long earliestCommentId = earliestCommentDO.getId();
                    commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliestCommentId, parentId);
                    redisTemplate.opsForValue().set(
                            RedisConstants.buildHaveFirstReplyCommentKey(parentId),
                            1,
                            RandomUtil.randomInt(5 * 60 * 60),
                            TimeUnit.SECONDS
                    );
                }
            }

            List<Long> alreadyHasIds = commentDOS.stream()
                    .filter(commentDO -> commentDO.getFirstReplyCommentId() != 0)
                    .map(CommentDO::getId)
                    .toList();

            if (CollUtil.isNotEmpty(alreadyHasIds)) {
                redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                    alreadyHasIds.forEach(id -> {
                        byte[] keyBytes = redisTemplate.getStringSerializer().serialize(RedisConstants.buildHaveFirstReplyCommentKey(id));
                        byte[] valueBytes = redisTemplate.getStringSerializer().serialize("1");
                        connection.setEx(keyBytes, RandomUtil.randomInt(5 * 60 * 60), valueBytes);
                    });
                    return null;
                });
            }
        }
    }

    private void syncOneLevelComment2RedisZSet(List<CommentBO> commentBOS) {
        Map<Long, List<CommentBO>> commentIdAndBOListMap = commentBOS.stream()
                .filter(commentBO -> Objects.equals(commentBO.getLevel(), CommentLevelEnum.ONE.getCode()))
                .collect(Collectors.groupingBy(CommentBO::getNoteId));

        commentIdAndBOListMap.forEach((noteId, commentBOList) -> {
            String key = RedisConstants.buildCommentListKey(noteId);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_hot_comments.lua")));
            script.setResultType(Long.class);

            List<Object> args = Lists.newArrayList();
            commentBOList.forEach(commentBO -> {
                args.add(commentBO.getId());
                args.add(0);
            });
            redisTemplate.execute(script, Collections.singletonList(key), args.toArray());
        });
    }

    private void deleteTwoLevelCommentRedisCache(List<CommentBO> commentBOS) {
        List<CommentBO> twoLevelComments = commentBOS.stream()
                .filter(commentBO -> Objects.equals(commentBO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .toList();

        if (CollUtil.isNotEmpty(twoLevelComments)) {
            Set<Long> parentCommentIds = twoLevelComments.stream()
                    .map(CommentBO::getParentId)
                    .collect(Collectors.toSet());

            redisTemplate.delete(parentCommentIds.stream()
                    .map(RedisConstants::buildChildCommentListKey)
                    .collect(Collectors.toList()));

            redisTemplate.delete(parentCommentIds.stream()
                    .map(RedisConstants::buildCommentDetailKey)
                    .collect(Collectors.toList()));

            parentCommentIds.forEach(parentId -> {
                rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, parentId, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {}
                    @Override
                    public void onException(Throwable throwable) {}
                });
            });
        }
    }
}