package com.wsz.xiaolanshu.comment.biz.consumer;

import com.wsz.xiaolanshu.comment.biz.constant.MQConstants;
import com.wsz.xiaolanshu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.apache.rocketmq.spring.annotation.MessageModel;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 16:06
 * @Company:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, // Group
        topic = MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, // 消费的主题 Topic
        messageModel = MessageModel.BROADCASTING) // 广播模式
public class DeleteCommentLocalCacheConsumer implements RocketMQListener<String> {

    @Resource
    private CommentService commentService;

    @Override
    public void onMessage(String body) {
        Long commentId = Long.valueOf(body);
        log.info("## 消费者消费成功, commentId: {}", commentId);

        commentService.deleteCommentLocalCache(commentId);
    }
}
