package com.wsz.xiaolanshu.note.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-21 19:59
 * @Company:
 */
@Data
public class AdminUpdateNoteStatusReqVO {
    @NotNull(message = "笔记ID不能为空")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    private Integer status;
}