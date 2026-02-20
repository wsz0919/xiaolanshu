package com.wsz.xiaolanshu.note.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:55
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindProfileNotePageListReqVO {

    /**
     * 类型：1：所有，2：收藏，3：点赞
     */
    private Integer type = 0;

    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

}

