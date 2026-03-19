package com.wsz.xiaolanshu.note.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNoteRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminUpdateNoteStatusReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindNotePageListReqVO;
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

    @PostMapping("/page")
    @ApiOperationLog(description = "后台获取笔记分页数据")
    @SaCheckPermission(value = "admin:note:list")
    public PageResponse<AdminNoteRspVO> findNotePageList(@Validated @RequestBody FindNotePageListReqVO findNotePageListReqVO) {
        return adminNoteService.findNotePageList(findNotePageListReqVO);
    }

    @PostMapping("/offline")
    @ApiOperationLog(description = "后台更新笔记状态（笔记审核）")
    @SaCheckPermission(value = "admin:note:offline")
    public Response<?> updateNoteStatus(@Validated @RequestBody AdminUpdateNoteStatusReqVO updateNoteStatusReqVO) {
        return adminNoteService.updateNoteStatus(updateNoteStatusReqVO);
    }

}
