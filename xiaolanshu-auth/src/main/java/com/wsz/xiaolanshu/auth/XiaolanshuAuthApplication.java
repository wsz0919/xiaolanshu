package com.wsz.xiaolanshu.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.wsz.xiaolanshu")
public class XiaolanshuAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolanshuAuthApplication.class, args);
    }

}
