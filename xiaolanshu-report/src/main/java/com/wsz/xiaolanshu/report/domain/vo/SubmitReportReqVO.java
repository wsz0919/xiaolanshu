package com.wsz.xiaolanshu.report.domain.vo;


import jakarta.validation.constraints.NotBlank;
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
public class SubmitReportReqVO {
    @NotNull(message = "举报对象类型不能为空")
    private Integer targetType; // 1:用户 2:笔记 3:评论

    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    private String reportReason;

    private String reportDetail;

    private String images;
}
