package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeNoteCollectMqDTO; // 请确保创建了这个DTO
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeNoteCollectConsumer 消费消息: {}", body);
        NoticeNoteCollectMqDTO dto = JsonUtils.parseObject(body, NoticeNoteCollectMqDTO.class);
        if (Objects.isNull(dto) || dto.getNoteCreatorId() == null) return;

        // 过滤自己收藏自己
        if (dto.getUserId().equals(dto.getNoteCreatorId())) return;

        // 1-收藏，0-取消收藏
        if (dto.getType() == 1) {
            NoticeDO notice = new NoticeDO();
            notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
            notice.setReceiverId(dto.getNoteCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(1);      // 1-赞和收藏
            notice.setSubType(12);  // 12-收藏笔记
            notice.setTargetId(dto.getNoteId());
            noticeDOMapper.insert(notice);
        } else {
            // 取消收藏 -> 删除对应通知
            noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getNoteCreatorId(), 1, dto.getNoteId());
        }
    }
}