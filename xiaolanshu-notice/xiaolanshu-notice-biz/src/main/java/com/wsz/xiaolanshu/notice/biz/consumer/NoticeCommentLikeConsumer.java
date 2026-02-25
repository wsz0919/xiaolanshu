package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticeCommentLikeMqDTO; // 需新建此DTO
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeCommentLikeConsumer 消费消息: {}", body);
        NoticeCommentLikeMqDTO dto = JsonUtils.parseObject(body, NoticeCommentLikeMqDTO.class);

        // 这里的 commentCreatorId 是指被点赞的评论的作者ID，必须由发送方传过来
        if (Objects.isNull(dto) || dto.getCommentCreatorId() == null) return;

        // 过滤自己赞自己
        if (dto.getUserId().equals(dto.getCommentCreatorId())) return;

        // 1-点赞，0-取消点赞
        if (dto.getType() == 1) {
            NoticeDO notice = new NoticeDO();
            notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
            notice.setReceiverId(dto.getCommentCreatorId());
            notice.setSenderId(dto.getUserId());
            notice.setType(1);      // 1-赞和收藏
            notice.setSubType(13);  // 13-点赞评论
            notice.setTargetId(dto.getCommentId());
            noticeDOMapper.insert(notice);
        } else {
            // 取消点赞评论 -> 删除通知
            noticeDOMapper.deleteByBusinessKey(dto.getUserId(), dto.getCommentCreatorId(), 1, dto.getCommentId());
        }
    }
}