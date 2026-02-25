package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
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
    private NoteFeignApi noteFeignApi;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeCommentConsumer 消费消息: {}", body);
        NoticePublishCommentMqDTO dto = JsonUtils.parseObject(body, NoticePublishCommentMqDTO.class);
        if (Objects.isNull(dto)) return;

        Long receiverId = null;

        // 1. 判断通知接收者
        if (dto.getReplyCommentId() != null && dto.getReplyCommentId() > 0) {
            // 情况A：回复了别人的评论 -> 通知被回复的人
            receiverId = dto.getReplyUserId();
        } else {
            // 情况B：直接评论了笔记 -> 通知笔记作者
            // 需要调用笔记服务查询作者 ID (通常发送端也会传，如果没传则查库兜底)
            FindNoteDetailReqDTO reqDTO = new FindNoteDetailReqDTO();
            reqDTO.setId(dto.getNoteId());
            FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(reqDTO).getData();
            if (note != null) {
                receiverId = note.getCreatorId();
            }
        }

        // 2. 校验：ID无效 或 自己评论自己 不发通知
        if (receiverId == null || receiverId == 0L || receiverId.equals(dto.getCreatorId())) {
            return;
        }

        // 3. 构建并插入通知
        NoticeDO notice = new NoticeDO();
        notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
        notice.setReceiverId(receiverId);
        notice.setSenderId(dto.getCreatorId());
        notice.setType(3); // 3-评论和@
        // 31-评论笔记，32-回复评论
        notice.setSubType(dto.getReplyCommentId() == null || dto.getReplyCommentId() == 0 ? 31 : 32);
        notice.setTargetId(dto.getCommentId()); // targetId 存当前这条评论的 ID

        noticeDOMapper.insert(notice);
    }
}