package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.user.relation.biz.domain.dto.FollowUserMqDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import java.util.Objects;

@Component
@RocketMQMessageListener(
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW, // 注意这里的 notice 标识
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        consumeMode = ConsumeMode.ORDERLY // 保持和您原有逻辑一样的顺序消费
)
@Slf4j
public class NoticeFollowConsumer implements RocketMQListener<Message> {

    @Resource
    private NoticeDOMapper noticeDOMapper;

    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;

    @Override
    public void onMessage(Message message) {
        String bodyJsonStr = new String(message.getBody());
        String tags = message.getTags();

        log.info("==> NoticeFollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 只有新增关注才发通知，取关不发通知
        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) {
            FollowUserMqDTO dto = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);
            if (Objects.isNull(dto)) return;

            NoticeDO notice = new NoticeDO();
            notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id"))); // 获取分布式ID
            notice.setReceiverId(dto.getFollowUserId()); // 被关注的人是接收者
            notice.setSenderId(dto.getUserId());         // 发起关注的人
            notice.setType(2); // 2-新增关注
            notice.setSubType(21);
            noticeDOMapper.insert(notice);
        }
    }
}