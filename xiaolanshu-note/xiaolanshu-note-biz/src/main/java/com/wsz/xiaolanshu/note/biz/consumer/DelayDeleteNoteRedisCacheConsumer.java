package com.wsz.xiaolanshu.note.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.note.biz.constant.MQConstants;
import com.wsz.xiaolanshu.note.biz.constant.RedisConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-16 16:11
 * @Company:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, // Group
        topic = MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE // 消费的主题 Topic
)
public class DelayDeleteNoteRedisCacheConsumer implements RocketMQListener<String>  {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        try {
            List<Long> noteIdAndUserId = JsonUtils.parseList(body, Long.class);

            Long noteId = noteIdAndUserId.get(0);
            Long userId = noteIdAndUserId.get(1);
            log.info("## 延迟消息消费成功, noteId: {}, userId: {}", noteId, userId);

            // 删除 Redis 笔记缓存
            String noteDetailRedisKey = RedisConstants.buildNoteDetailKey(noteId);
            // 删除个人主页 - 已发布笔记列表缓存
            String publishedNoteListRedisKey = RedisConstants.buildPublishedNoteListKey(userId);
            // 批量删除
            redisTemplate.delete(Arrays.asList(noteDetailRedisKey, publishedNoteListRedisKey));
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
