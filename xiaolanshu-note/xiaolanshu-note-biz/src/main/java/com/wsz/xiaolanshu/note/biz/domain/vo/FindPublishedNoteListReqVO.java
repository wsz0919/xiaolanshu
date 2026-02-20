package com.wsz.xiaolanshu.note.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:11
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindPublishedNoteListReqVO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 游标，即笔记 ID，用于分页使用
     */
    private Long cursor;

}
