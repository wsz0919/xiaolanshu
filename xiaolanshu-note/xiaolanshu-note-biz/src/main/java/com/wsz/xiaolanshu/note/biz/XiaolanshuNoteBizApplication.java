package com.wsz.xiaolanshu.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-10 17:55
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.note.biz.mapper")
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuNoteBizApplication.class, args);
    }

}
