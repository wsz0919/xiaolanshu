package com.wsz.xiaolanshu.auth.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.auth.domain.vo.UpdatePasswordReqVO;
import com.wsz.xiaolanshu.auth.domain.vo.UserLoginReqVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-24 15:14
 * @Company:
 */
public interface AuthService {

    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 退出登录
     * @return
     */
    Response<?> logout();

    /**
     * 修改密码
     * @param updatePasswordReqVO
     * @return
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}
