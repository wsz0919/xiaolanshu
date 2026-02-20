package com.wsz.xiaolanshu.auth.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.auth.domain.vo.SendVerificationCodeReqVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025/11/21 23:35
 * @Company:
 */
public interface VerificationCodeService {
    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
