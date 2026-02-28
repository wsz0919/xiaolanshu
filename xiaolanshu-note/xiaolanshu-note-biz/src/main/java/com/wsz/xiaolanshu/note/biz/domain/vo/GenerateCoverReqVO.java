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
 * @Date 2026-02-28 21:27
 * @Company:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateCoverReqVO {

    @NotNull(message = "模板 ID 不能为空")
    private Long templateId; // 模板ID

    @NotNull(message = "标题不能为空")
    private String title;       // 用户输入的标题
}