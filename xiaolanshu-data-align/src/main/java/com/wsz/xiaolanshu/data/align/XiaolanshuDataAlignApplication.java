package com.wsz.xiaolanshu.data.align;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-02 14:11
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.data.align.mapper")
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuDataAlignApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuDataAlignApplication.class, args);
    }

}
