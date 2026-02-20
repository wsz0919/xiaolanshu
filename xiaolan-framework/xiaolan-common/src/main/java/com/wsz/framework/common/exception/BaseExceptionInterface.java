package com.wsz.framework.common.exception;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025/11/21 20:33
 * @Company:
 */
public interface BaseExceptionInterface {

    // 获取异常码
    String getErrorCode();

    // 获取异常信息
    String getErrorMessage();
}


