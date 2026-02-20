package com.wsz.xiaolanshu.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-04 13:33
 * @Company:
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.wsz.xiaolanshu.search.mapper")
public class XiaolanshuSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuSearchApplication.class, args);
    }

}
