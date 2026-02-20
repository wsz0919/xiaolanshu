package com.wsz.xiaolanshu.note.biz.consumer;

import com.wsz.xiaolanshu.note.biz.constant.MQConstants;
import com.wsz.xiaolanshu.note.biz.constant.RedisConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 17:08
 * @Company:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, // Group
        topic = MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE // 消费的主题 Topic
)
public class DelayDeletePublishedNoteListRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        Long userId = Long.valueOf(body);

        // 删除个人主页 - 已发布笔记列表缓存
        String publishedNoteListRedisKey = RedisConstants.buildPublishedNoteListKey(userId);

        // 批量删除
        redisTemplate.delete(publishedNoteListRedisKey);
    }
}
