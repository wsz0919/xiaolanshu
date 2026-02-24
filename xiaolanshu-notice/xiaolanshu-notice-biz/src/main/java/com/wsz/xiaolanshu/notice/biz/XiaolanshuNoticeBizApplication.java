package com.wsz.xiaolanshu.notice.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 14:35
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.notice.biz.mapper")
public class XiaolanshuNoticeBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuNoticeBizApplication.class, args);
    }
}
