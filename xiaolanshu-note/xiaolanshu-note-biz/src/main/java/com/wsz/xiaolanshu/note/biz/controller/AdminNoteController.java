package com.wsz.xiaolanshu.note.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminOfflineNoteReqVO;
import com.wsz.xiaolanshu.note.biz.service.NoteService;
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
 * @Date 2026-03-16 10:29
 * @Company:
 */
@RestController
@RequestMapping("/admin/note")
public class AdminNoteController {

    @Resource
    private NoteService noteService;

    /**
     * 强制下架违规笔记
     */
    @PostMapping("/offline")
    @ApiOperationLog(description = "管理员下架笔记")
    // 使用我们昨天初始化的权限 Key，超级管理员和有权限的运营可以访问
    @SaCheckPermission(value = "admin:note:offline", orRole = "super_admin")
    public Response<?> offlineNote(@Validated @RequestBody AdminOfflineNoteReqVO reqVO) {
        // 这里调用你的 NoteService 去更新数据库 t_note 的 status 为 3 (被下架)
        // 伪代码: noteService.updateStatus(reqVO.getNoteId(), 3);

        // TODO: 如果需要，可以发一条 MQ 消息给 xiaolanshu-notice 服务，通知用户他的笔记被下架了

        return Response.success("笔记下架成功");
    }

    /**
     * 分页查询所有笔记 (后台用，无需权限校验只要有后台角色即可进入，网关已拦截 /admin/**)
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "管理员分页查询笔记")
    @SaCheckPermission(value = "admin:note:list", orRole = "super_admin")
    public Response<?> getNotePageList() {
        // TODO: 调用 NoteService 实现后台的分页条件查询 (可按发布者、状态筛选)
        return Response.success(null);
    }
}
