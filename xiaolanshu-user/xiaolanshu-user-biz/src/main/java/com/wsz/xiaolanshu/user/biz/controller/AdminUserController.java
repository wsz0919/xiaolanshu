package com.wsz.xiaolanshu.user.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.domain.dataobject.UserDO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUpdateUserStatusReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageRspVO;
import com.wsz.xiaolanshu.user.biz.mapper.UserDOMapper;
import com.wsz.xiaolanshu.user.biz.service.AdminUserService;
import com.wsz.xiaolanshu.user.biz.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:33
 * @Company:
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    /**
     * 封禁/解封 用户
     */
    @PostMapping("/ban")
    @ApiOperationLog(description = "封禁/解封用户")
    @SaCheckPermission(value = "admin:user:ban", orRole = "super_admin")
    public Response<?> banUser(@Validated @RequestBody AdminUpdateUserStatusReqVO reqVO) {
        return adminUserService.banUser(reqVO);
    }

    /**
     * 分页查询所有用户
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "管理员分页查询用户列表")
    @SaCheckPermission(value = "admin:user:list", orRole = "super_admin")
    public PageResponse<AdminUserPageRspVO> getUserPageList(@Validated @RequestBody AdminUserPageReqVO reqVO) {
        return adminUserService.getUserPageList(reqVO);
    }
}
