package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeNoteLikeMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeNoteLikeConsumer 消费消息: {}", body);
        NoticeNoteLikeMqDTO dto = JsonUtils.parseObject(body, NoticeNoteLikeMqDTO.class);
        if (Objects.isNull(dto) || dto.getNoteCreatorId() == null) return;

        // 过滤自己给自己点赞
        if (dto.getUserId().equals(dto.getNoteCreatorId())) return;

        // 1-点赞，0-取消点赞
        if (dto.getType() == 1) {
            NoticeDO notice = new NoticeDO();
            notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
            notice.setReceiverId(dto.getNoteCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(1);      // 1-赞和收藏
            notice.setSubType(11);  // 11-点赞笔记
            notice.setTargetId(dto.getNoteId());
            noticeDOMapper.insert(notice);
        } else {
            // 取消点赞 -> 删除对应通知
            noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), 1, dto.getNoteId());
        }
    }
}