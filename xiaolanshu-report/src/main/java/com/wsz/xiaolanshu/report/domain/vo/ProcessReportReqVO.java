package com.wsz.xiaolanshu.report.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:41
 * @Company:
 */
@Data
public class ProcessReportReqVO {
    @NotNull(message = "举报记录ID不能为空")
    private Long reportId;

    @NotNull(message = "处理状态不能为空")
    private Integer status; // 1:已处理-违规 2:已处理-不违规/驳回

    private String processRemark; // 处理备注(仅后台可见)
}
