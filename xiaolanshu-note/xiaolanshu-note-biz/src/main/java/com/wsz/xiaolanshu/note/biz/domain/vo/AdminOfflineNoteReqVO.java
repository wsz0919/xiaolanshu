package com.wsz.xiaolanshu.note.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:28
 * @Company:
 */
@Data
public class AdminOfflineNoteReqVO {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    // 可以加一个下架原因字段，后续做通知用
    private String reason;
}
