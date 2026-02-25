package com.wsz.xiaolanshu.notice.biz.consumer;

import cn.hutool.core.util.RandomUtil;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeNoteLikeMqDTO;
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
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE
)
@Slf4j
public class NoticeNoteLikeConsumer implements RocketMQListener<String> {

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeNoteLikeConsumer 消费消息: {}", body);
        NoticeNoteLikeMqDTO dto = JsonUtils.parseObject(body, NoticeNoteLikeMqDTO.class);
        if (Objects.isNull(dto) || dto.getNoteCreatorId() == null) return;

        // 过滤自己给自己点赞
        if (dto.getUserId().equals(dto.getNoteCreatorId())) return;

        Integer type = 1; // 1-赞和收藏 Tab
        String redisKey = RedisConstants.buildNoticeZSetKey(dto.getNoteCreatorId(), type);

        if (dto.getType() == 1) { // 1-点赞
            Long noticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
            NoticeDO notice = new NoticeDO();
            notice.setId(noticeId);
            notice.setReceiverId(dto.getNoteCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(type);
            notice.setSubType(11); // 11-点赞笔记
            notice.setTargetId(dto.getNoteId());

            // 入库
            noticeDOMapper.insert(notice);
            // 写缓存
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(noticeId), System.currentTimeMillis());
            long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            // 限制容量，只保留最近 500 条
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -501);

        } else { // 0-取消点赞
            // 先查出之前那条通知的 ID (为了从 Redis 删它)
            Long noticeId = noticeDOMapper.selectNoticeIdByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), type, dto.getNoteId());
            if (noticeId != null) {
                // 删库并删缓存
                noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), type, dto.getNoteId());
                redisTemplate.opsForZSet().remove(redisKey, String.valueOf(noticeId));
            }
        }
    }
}