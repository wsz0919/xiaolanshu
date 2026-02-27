package com.wsz.xiaolanshu.notice.biz.consumer;

import cn.hutool.core.util.RandomUtil;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeCommentLikeMqDTO;
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
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE
)
@Slf4j
public class NoticeCommentLikeConsumer implements RocketMQListener<String> {

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeCommentLikeConsumer 消费消息: {}", body);
        NoticeCommentLikeMqDTO dto = JsonUtils.parseObject(body, NoticeCommentLikeMqDTO.class);
        if (Objects.isNull(dto) || dto.getCommentCreatorId() == null) return;
        if (dto.getUserId().equals(dto.getCommentCreatorId())) return;

        Integer type = 1; // 1-赞和收藏
        String redisKey = RedisConstants.buildNoticeZSetKey(dto.getCommentCreatorId(), type);

        if (dto.getType() == 1) { // 1-点赞评论
            Long noticeId = Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"));
            NoticeDO notice = new NoticeDO();
            notice.setId(noticeId);
            notice.setReceiverId(dto.getCommentCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(type);
            notice.setSubType(13); // 13-点赞评论
            notice.setTargetId(dto.getCommentId());

            noticeDOMapper.insert(notice);
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(noticeId), System.currentTimeMillis());
            long expireSeconds = 60 * 60 * 24 * 7 + RandomUtil.randomInt(60 * 60);
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForZSet().removeRange(redisKey, 0, -501);

        } else { // 0-取消点赞评论
            Long noticeId = noticeDOMapper.selectNoticeIdByBusinessKey(dto.getUserId(), dto.getCommentCreatorId(), 13, dto.getCommentId());
            if (noticeId != null) {
                noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getCommentCreatorId(), 13, dto.getCommentId());
                redisTemplate.opsForZSet().remove(redisKey, String.valueOf(noticeId));
            }
        }
    }
}