package com.wsz.framework.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025/11/21 20:33
 * @Company:
 */
@Getter
@Setter
public class BizException extends RuntimeException {
    // 异常码
    private String errorCode;
    // 错误信息
    private String errorMessage;

    public BizException(BaseExceptionInterface baseExceptionInterface) {
        this.errorCode = baseExceptionInterface.getErrorCode();
        this.errorMessage = baseExceptionInterface.getErrorMessage();
    }
}