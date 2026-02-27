package com.wsz.xiaolanshu.notice.biz.consumer;

import cn.hutool.core.util.RandomUtil;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeNoteCollectMqDTO;
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
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
        topic = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT
)
@Slf4j
public class NoticeNoteCollectConsumer implements RocketMQListener<String> {

    @Resource
    private NoticeDOMapper noticeDOMapper;

    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeNoteCollectConsumer 消费消息: {}", body);
        NoticeNoteCollectMqDTO dto = JsonUtils.parseObject(body, NoticeNoteCollectMqDTO.class);
        if (Objects.isNull(dto) || dto.getNoteCreatorId() == null) return;
        if (dto.getUserId().equals(dto.getNoteCreatorId())) return;

        Integer type = 1; // 1-赞和收藏
        String redisKey = RedisConstants.buildNoticeZSetKey(dto.getNoteCreatorId(), type);

        if (dto.getType() == 1) { // 1-收藏
            Long noticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
            NoticeDO notice = new NoticeDO();
            notice.setId(noticeId);
            notice.setReceiverId(dto.getNoteCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(type);
            notice.setSubType(12); // 12-收藏笔记
            notice.setTargetId(dto.getNoteId());

            noticeDOMapper.insert(notice);
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(noticeId), System.currentTimeMillis());
            long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -501);

        } else { // 0-取消收藏
            Long noticeId = noticeDOMapper.selectNoticeIdByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), 12, dto.getNoteId());
            if (noticeId != null) {
                noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), 12, dto.getNoteId());
                redisTemplate.opsForZSet().remove(redisKey, String.valueOf(noticeId));
            }
        }
    }
}