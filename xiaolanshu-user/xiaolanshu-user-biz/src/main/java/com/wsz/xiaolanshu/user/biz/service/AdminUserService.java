package com.wsz.xiaolanshu.user.biz.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUpdateUserStatusReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageRspVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 15:29
 * @Company:
 */
public interface AdminUserService {

    /**
     * 封禁/解封 用户
     */
    Response<?> banUser(AdminUpdateUserStatusReqVO reqVO);

    /**
     * 管理员分页查询用户列表
     */
    PageResponse<AdminUserPageRspVO> getUserPageList(AdminUserPageReqVO reqVO);
}
