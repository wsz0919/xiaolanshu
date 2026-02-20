package com.wsz.xiaolanshu.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.biz.constant.MQConstants;
import com.wsz.xiaolanshu.biz.domain.dto.PublishNoteDTO;
import com.wsz.xiaolanshu.biz.service.NoteContentService;
import com.wsz.xiaolanshu.kv.dto.req.AddNoteContentReqDTO;
import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 17:43
 * @Company:
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION, // Group 组
        topic = MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION // 消费的主题 Topic
)
@Slf4j
public class SaveNoteContentConsumer implements RocketMQListener<Message> {

    @Resource
    private NoteContentService noteContentService;

    @Override
    public void onMessage(Message message) {
        // 消息体
        String bodyJsonStr = new String(message.getBody());

        log.info("## SaveNoteContentConsumer 消费了事务消息 {}", bodyJsonStr);

        // 笔记正文保存到 Cassandra 中
        if (StringUtils.isNotBlank(bodyJsonStr)) {
            PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(bodyJsonStr, PublishNoteDTO.class);
            String content = publishNoteDTO.getContent();
            String uuid = publishNoteDTO.getContentUuid();


            noteContentService.addNoteContent(AddNoteContentReqDTO.builder()
                    .uuid(uuid)
                    .content(content)
                    .build());
        }
    }

}

