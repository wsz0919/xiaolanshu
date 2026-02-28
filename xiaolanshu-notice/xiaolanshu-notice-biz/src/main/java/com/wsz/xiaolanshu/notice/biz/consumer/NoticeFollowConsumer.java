package com.wsz.xiaolanshu.notice.biz.consumer;

import cn.hutool.core.util.RandomUtil;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.FollowUserMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW
)
@Slf4j
public class NoticeFollowConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody());
        String tags = message.getTags();
        log.info("==> NoticeFollowConsumer 消费消息, Tags: {}, Body: {}", tags, body);

        FollowUserMqDTO dto = JsonUtils.parseObject(body, FollowUserMqDTO.class);
        if (Objects.isNull(dto)) return;

        Integer type = 2; // 2-新增关注
        String redisKey = RedisConstants.buildNoticeZSetKey(dto.getFollowUserId(), type);

        if (MQConstants.TAG_FOLLOW.equals(tags)) { // 关注
            Long noticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
            NoticeDO notice = new NoticeDO();
            notice.setId(noticeId);
            notice.setReceiverId(dto.getFollowUserId());
            notice.setSenderId(dto.getUserId());
            notice.setType(type);
            notice.setSubType(21); // 21-关注了你
            notice.setTargetId(0L); // 关注不需要特定的 targetId

            noticeDOMapper.insert(notice);
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(noticeId), System.currentTimeMillis());
            long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -501);

        } else if (MQConstants.TAG_UNFOLLOW.equals(tags)) { // 取消关注
            Long noticeId = noticeDOMapper.selectNoticeIdByBusinessKey(dto.getUserId(), dto.getUnfollowUserId(), 21, 0L);
            if (noticeId != null) {
                noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getUnfollowUserId(), 21, 0L);
                redisTemplate.opsForZSet().remove(redisKey, String.valueOf(noticeId));
            }
        }
    }
}