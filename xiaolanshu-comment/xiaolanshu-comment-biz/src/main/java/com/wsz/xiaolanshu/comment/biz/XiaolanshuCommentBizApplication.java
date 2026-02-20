package com.wsz.xiaolanshu.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 13:53
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.comment.biz.mapper")
@EnableRetry
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuCommentBizApplication.class, args);
    }

}
