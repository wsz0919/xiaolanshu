package com.wsz.xiaolanshu.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 21:10
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.user.biz.mapper")
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuUserBizApplication.class, args);
    }
}
