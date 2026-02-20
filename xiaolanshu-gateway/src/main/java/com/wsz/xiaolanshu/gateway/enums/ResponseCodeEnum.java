package com.wsz.xiaolanshu.gateway.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-26 16:43
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {
    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("500", "系统繁忙，请稍后再试"),
    UNAUTHORIZED("401", "权限不足"),


    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
