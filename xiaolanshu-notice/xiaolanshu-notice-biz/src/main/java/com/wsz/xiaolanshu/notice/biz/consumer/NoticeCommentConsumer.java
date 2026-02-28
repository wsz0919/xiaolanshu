package com.wsz.xiaolanshu.notice.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticePublishCommentMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_PUBLISH_COMMENT,
        topic = MQConstants.TOPIC_PUBLISH_COMMENT
)
@Slf4j
public class NoticeCommentConsumer implements RocketMQListener<String> {

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;
    @Resource
    private NoteFeignApi noteFeignApi;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeCommentConsumer 消费消息: {}", body);
        NoticePublishCommentMqDTO dto = JsonUtils.parseObject(body, NoticePublishCommentMqDTO.class);
        if (Objects.isNull(dto)) return;

        // ================== 1. 处理常规的【评论/回复】通知 ==================
        Long receiverId = null;

        // 判断通知应该发给谁
        if (dto.getReplyCommentId() != null && dto.getReplyCommentId() > 0) {
            // 这是回复评论 -> 发给被回复的人
            receiverId = dto.getReplyUserId();
        } else {
            // 这是直接评论笔记 -> 发给笔记作者
            FindNoteDetailReqDTO reqDTO = new FindNoteDetailReqDTO();
            reqDTO.setId(dto.getNoteId());
            FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(reqDTO).getData();
            if (note != null) {
                receiverId = note.getCreatorId();
            }
        }

        // 防御性拦截（修改为块级拦截，不能 return，否则下面的 @ 逻辑不会执行）
        if (receiverId != null && receiverId > 0L && !receiverId.equals(dto.getCreatorId())) {
            Integer type = 3; // 3-评论和@
            String redisKey = RedisConstants.buildNoticeZSetKey(receiverId, type);

            // 构造常规通知对象
            Long noticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
            NoticeDO notice = new NoticeDO();
            notice.setId(noticeId);
            notice.setReceiverId(receiverId);
            notice.setSenderId(dto.getCreatorId());
            notice.setType(type);
            notice.setSubType(dto.getReplyCommentId() == null || dto.getReplyCommentId() == 0 ? 31 : 32);
            notice.setTargetId(dto.getCommentId());

            // 入库并写缓存
            noticeDOMapper.insert(notice);
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(noticeId), System.currentTimeMillis());
            long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -501);
        }

        // ================== 2. 处理【@ 提到我】的通知 ==================
        if (CollUtil.isNotEmpty(dto.getMentionUserIds())) {
            for (Long mentionedUserId : dto.getMentionUserIds()) {
                // 防御性拦截：防止自己 @ 自己产生通知
                if (Objects.equals(mentionedUserId, dto.getCreatorId())) continue;

                Long mentionNoticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
                NoticeDO mentionNotice = new NoticeDO();
                mentionNotice.setId(mentionNoticeId);
                mentionNotice.setReceiverId(mentionedUserId); // 接收者是被 @ 的人
                mentionNotice.setSenderId(dto.getCreatorId());
                mentionNotice.setType(3);     // 3 代表评论和@类 Tab
                mentionNotice.setSubType(33); // 33 代表 "@了你"
                mentionNotice.setTargetId(dto.getCommentId()); // 目标依然是这条评论，前端复用获取评论内容的逻辑

                // 入库
                noticeDOMapper.insert(mentionNotice);

                // 写缓存 (注意 Key 的 receiverId 变了)
                String mentionRedisKey = RedisConstants.buildNoticeZSetKey(mentionedUserId, 3);
                redisTemplate.opsForZSet().add(mentionRedisKey, String.valueOf(mentionNoticeId), System.currentTimeMillis());
                long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
                redisTemplate.expire(mentionRedisKey, expireSeconds, TimeUnit.SECONDS);
                // 保证 ZSet 容量不过载 (最多保留500条最新通知)
                redisTemplate.opsForZSet().removeRange(mentionRedisKey, 0, -501);
            }
        }
    }
}