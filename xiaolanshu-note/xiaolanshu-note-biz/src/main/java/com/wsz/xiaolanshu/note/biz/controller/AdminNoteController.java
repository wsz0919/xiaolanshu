package com.wsz.xiaolanshu.note.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminOfflineNoteReqVO;
import com.wsz.xiaolanshu.note.biz.service.AdminNoteService;
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
    private AdminNoteService adminNoteService;

    /**
     * 强制下架违规笔记
     */
    @PostMapping("/offline")
    @ApiOperationLog(description = "管理员下架笔记")
    @SaCheckPermission(value = "admin:note:offline", orRole = "super_admin")
    public Response<?> offlineNote(@Validated @RequestBody AdminOfflineNoteReqVO reqVO) {
        return adminNoteService.offlineNote(reqVO);
    }

    /**
     * 分页查询所有笔记 (后台用，无需权限校验只要有后台角色即可进入，网关已拦截 /admin/**)
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "管理员分页查询笔记")
    @SaCheckPermission(value = "admin:note:list", orRole = "super_admin")
    public PageResponse<AdminNotePageRspVO.AdminNoteItemRspVO> getNotePageList(@RequestBody AdminNotePageReqVO reqVO) {
        return adminNoteService.getNotePageList(reqVO);
    }
}
