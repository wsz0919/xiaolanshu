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
 * @Date 2026-01-29 14:46
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeNoteReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;

}
