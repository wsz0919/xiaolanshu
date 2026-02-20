package com.wsz.xiaolanshu.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-01-28 15:00
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.count.biz.mapper")
public class XiaolanshuCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuCountBizApplication.class, args);
    }

}
