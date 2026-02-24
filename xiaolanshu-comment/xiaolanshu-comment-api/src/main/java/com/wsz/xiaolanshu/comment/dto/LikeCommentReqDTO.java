package com.wsz.xiaolanshu.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 17:09
 * @Company:
 */
@Data
public class LikeCommentReqDTO {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;
}
