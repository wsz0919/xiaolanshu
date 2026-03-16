package com.wsz.xiaolanshu.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wsz.framework.common.response.Response;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.xiaolanshu.report.domain.vo.SubmitReportReqVO;
import com.wsz.xiaolanshu.report.service.ReportService;
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
 * @Date 2026-03-16 10:45
 * @Company:
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping("/submit")
    @ApiOperationLog(description = "用户提交举报")
    @SaCheckPermission(value = "app:report:submit", orRole = "super_admin")
    public Response<?> submitReport(@Validated @RequestBody SubmitReportReqVO reqVO) {
        return reportService.submitReport(reqVO);
    }
}