package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.FollowUserMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody());
        String tags = message.getTags();
        log.info("==> NoticeFollowConsumer 消费消息, Tags: {}, Body: {}", tags, body);

        FollowUserMqDTO dto = JsonUtils.parseObject(body, FollowUserMqDTO.class);
        if (Objects.isNull(dto)) return;

        // 1. 关注事件
        if (MQConstants.TAG_FOLLOW.equals(tags)) {
            NoticeDO notice = new NoticeDO();
            notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
            notice.setReceiverId(dto.getFollowUserId()); // 被关注的人是接收者
            notice.setSenderId(dto.getUserId());         // 发起关注的人是发送者
            notice.setType(2);      // 2-新增关注
            notice.setSubType(21);  // 21-关注了你
            notice.setTargetId(0L); // 关注不需要特定的 targetId，也可存 0
            noticeDOMapper.insert(notice);
        }
        // 2. 取关事件
        else if (MQConstants.TAG_UNFOLLOW.equals(tags)) {
            // 取消关注 -> 删除对应通知
            noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getUnfollowUserId(), 2, 0L);
        }
    }
}