package com.wsz.xiaolanshu.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.report.domain.vo.ProcessReportReqVO;
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
 * @Date 2026-03-16 11:08
 * @Company:
 */
@RestController
@RequestMapping("/admin/report")
public class AdminReportController {
    @Resource
    private ReportService reportService;

    @PostMapping("/process")
    @ApiOperationLog(description = "管理员处理举报")
    @SaCheckPermission(value = "admin:report:process", orRole = "super_admin")
    public Response<?> processReport(@Validated @RequestBody ProcessReportReqVO reqVO) {
        return reportService.processReport(reqVO);
    }
}
