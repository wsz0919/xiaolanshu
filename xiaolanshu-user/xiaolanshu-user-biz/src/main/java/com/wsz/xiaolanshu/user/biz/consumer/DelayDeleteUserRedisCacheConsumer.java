package com.wsz.xiaolanshu.user.biz.consumer;

import com.wsz.xiaolanshu.user.biz.constant.MQConstants;
import com.wsz.xiaolanshu.user.biz.constant.RedisConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 15:58
 * @Company:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaolanshu_group_" + MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE, // Group
        topic = MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE // 消费的主题 Topic
)
public class DelayDeleteUserRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        Long userId = Long.valueOf(body);
        log.info("## 延迟消息消费成功, userId: {}", userId);

        // 删除 Redis 用户缓存
        String userInfoRedisKey = RedisConstants.buildUserInfoKey(userId);
        String userProfileRedisKey = RedisConstants.buildUserProfileKey(userId);
        // 批量删除
        redisTemplate.delete(Arrays.asList(userInfoRedisKey, userProfileRedisKey));
    }
}
