package com.wsz.xiaolanshu.report.domain.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 11:38
 * @Company:
 */
@Data
public class FindReportPageListReqVO {
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer pageSize = 10;

    private Integer status; // 按状态筛选 (可选)
}
