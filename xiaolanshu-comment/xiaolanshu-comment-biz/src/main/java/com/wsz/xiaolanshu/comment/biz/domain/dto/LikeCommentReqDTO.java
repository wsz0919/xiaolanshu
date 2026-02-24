package com.wsz.xiaolanshu.comment.biz.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 15:17
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeCommentReqDTO {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;

}
