package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.comment.biz.domain.dto.PublishCommentMqDTO;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticePublishCommentMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:44
 * @Company:
 */
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
    private NoteFeignApi noteFeignApi; // 注入您的笔记 FeignApi

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeCommentConsumer 消费了消息: {}", body);
        NoticePublishCommentMqDTO dto = JsonUtils.parseObject(body, NoticePublishCommentMqDTO.class);
        if (Objects.isNull(dto)) return;

        Long receiverId = null;

        // 核心逻辑：判断是【回复别人的评论】还是【直接评论笔记】
        if (dto.getReplyCommentId() != null && dto.getReplyCommentId() > 0) {
            // 情况1：这是在回复别人的评论，通知接收者就是被回复人
            receiverId = dto.getReplyUserId();
        } else {
            // 情况2：这是直接评论笔记，需要通知笔记的作者
            FindNoteDetailReqDTO reqDTO = new FindNoteDetailReqDTO();
            reqDTO.setId(dto.getNoteId());
            FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(reqDTO).getData();
            if (note != null) {
                receiverId = note.getCreatorId();
            }
        }

        // 防御性校验：自己不通知自己，且接收者不能为空或 0
        if (receiverId == null || receiverId == 0L || receiverId.equals(dto.getCreatorId())) {
            return;
        }

        // 入库
        NoticeDO notice = new NoticeDO();
        notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
        notice.setReceiverId(receiverId);
        notice.setSenderId(dto.getCreatorId());
        notice.setType(3); // 3-评论和@
        notice.setSubType(dto.getReplyCommentId() == null || dto.getReplyCommentId() == 0 ? 31 : 32);
        notice.setTargetId(dto.getCommentId());
        noticeDOMapper.insert(notice);
    }
}
