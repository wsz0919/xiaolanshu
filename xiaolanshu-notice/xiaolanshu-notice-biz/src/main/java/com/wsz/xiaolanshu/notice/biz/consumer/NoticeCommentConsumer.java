package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.dto.NoticePublishCommentMqDTO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message; // 引入 Message 类
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import java.util.Objects;

@Component
@RocketMQMessageListener(
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_PUBLISH_COMMENT,
        topic = MQConstants.TOPIC_PUBLISH_COMMENT
)
@Slf4j
public class NoticeCommentConsumer implements RocketMQListener<Message> { // ★ 这里改为 Message

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;
    @Resource
    private NoteFeignApi noteFeignApi;

    @Override
    public void onMessage(Message message) { // ★ 这里接收 Message
        // ★ 核心修改：手动从 Message 中提取 body 字节，并转为真正的 JSON 字符串
        String bodyJsonStr = new String(message.getBody());

        log.info("==> NoticeCommentConsumer 消费了消息: {}", bodyJsonStr);

        // 使用真正的 JSON 字符串进行反序列化
        NoticePublishCommentMqDTO dto = JsonUtils.parseObject(bodyJsonStr, NoticePublishCommentMqDTO.class);
        if (Objects.isNull(dto)) return;

        Long receiverId = null;

        // 判断是【回复别人的评论】还是【直接评论笔记】
        if (dto.getReplyCommentId() != null && dto.getReplyCommentId() > 0) {
            receiverId = dto.getReplyUserId();
        } else {
            FindNoteDetailReqDTO reqDTO = new FindNoteDetailReqDTO();
            reqDTO.setId(dto.getNoteId());
            // 注意：这里请确认您 NoteFeignApi 中对应的查询接口方法名
            FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(reqDTO).getData();
            if (note != null) {
                receiverId = note.getCreatorId();
            }
        }

        // 防御性校验
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