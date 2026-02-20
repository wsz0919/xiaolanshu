package com.wsz.framework.biz.operationlog.aspect;

import java.lang.annotation.*;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025/11/21 20:49
 * @Company:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {
    /**
     * API 功能描述
     *
     * @return
     */
    String description() default "";

}