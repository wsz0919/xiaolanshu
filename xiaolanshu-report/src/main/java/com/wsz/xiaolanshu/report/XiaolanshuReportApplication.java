package com.wsz.xiaolanshu.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 11:00
 * @Company:
 */
@SpringBootApplication
@MapperScan(basePackages = "com.wsz.xiaolanshu.report.mapper")
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuReportApplication.class, args);
    }
}
