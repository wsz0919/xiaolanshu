package com.wsz.xiaolanshu.note.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员下架笔记请求参数
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 14:00
 * @Company:
 */
@Data
public class AdminOfflineNoteReqVO {
    /**
     * 笔记ID
     */
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
}