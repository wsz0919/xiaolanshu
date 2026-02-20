package com.wsz.xiaolanshu.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-16 16:35
 * @Company:
 */
@SpringBootApplication
@MapperScan("com.wsz.xiaolanshu.user.relation.biz.mapper")
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuUserRelationBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuUserRelationBizApplication.class, args);
    }

}
