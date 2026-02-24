package com.wsz.xiaolanshu.notice.biz.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 14:50
 * @Company:
 */
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
