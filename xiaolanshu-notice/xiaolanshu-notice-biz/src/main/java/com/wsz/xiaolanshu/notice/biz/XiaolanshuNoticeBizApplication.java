package com.wsz.xiaolanshu.notice.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 14:35
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.notice.biz.mapper")
@EnableFeignClients("com.wsz.xiaolanshu")
public class XiaolanshuNoticeBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuNoticeBizApplication.class, args);
    }
}
