package com.wsz.xiaolanshu.user.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUpdateUserStatusReqVO;
import com.wsz.xiaolanshu.user.biz.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private UserService userService;

    /**
     * 封禁/解封 用户
     */
    @PostMapping("/status/update")
    @ApiOperationLog(description = "管理员更新用户状态(封禁/解封)")
    @SaCheckPermission(value = "admin:user:ban", orRole = "super_admin")
    public Response<?> updateUserStatus(@Validated @RequestBody AdminUpdateUserStatusReqVO reqVO) {

        // 1. 调用 UserService 更新 t_user 表的 status 字段
        // 伪代码: userService.updateUserStatus(reqVO.getUserId(), reqVO.getStatus());

        // 2. 关键：如果用户被封禁（status == 1），需要强制他下线！
        if (reqVO.getStatus() == 1) {
            // Sa-Token 提供的踢人下线功能，被踢的用户继续请求会抛出 NotLoginException(被踢下线)
            StpUtil.kickout(reqVO.getUserId());
        }

        return Response.success("用户状态更新成功");
    }

    /**
     * 分页查询所有用户 (后台大盘)
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "管理员分页查询用户列表")
    @SaCheckPermission(value = "admin:user:list", orRole = "super_admin")
    public Response<?> getUserPageList() {
        // TODO: 调用 UserService 实现后台的分页查询 (可按手机号、小蓝书号、状态搜索)
        return Response.success(null);
    }
}
