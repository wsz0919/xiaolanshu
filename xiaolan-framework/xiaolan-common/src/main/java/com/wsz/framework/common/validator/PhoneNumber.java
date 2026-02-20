package com.wsz.framework.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-24 13:14
 * @Company:
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {

    String message() default "手机号格式不正确, 需为 11 位数字";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
