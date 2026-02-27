package com.wsz.xiaolanshu.comment.biz.config; // 注意：notice 模块请修改对应的 package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 修改后的 Redis 配置
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置 RedisTemplate 的连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);

        // 1. Key 序列化：使用 StringRedisSerializer，确保 key 在工具中可见
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // 2. Value 序列化：改为 StringRedisSerializer [修改点]
        // 这样 redisTemplate.opsForValue().get() 返回的就是 JSON 字符串，
        // 从而匹配 CommentServiceImpl 中强转 (String) 的逻辑。
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}