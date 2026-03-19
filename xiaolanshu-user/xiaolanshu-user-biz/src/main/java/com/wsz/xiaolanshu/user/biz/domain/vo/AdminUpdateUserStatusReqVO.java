package com.wsz.xiaolanshu.user.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:33
 * @Company:
 */
@Data
public class AdminUpdateUserStatusReqVO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0:启用 1:禁用
}
