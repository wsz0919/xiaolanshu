package com.wsz.xiaolanshu.comment.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-26 20:32
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeStatusDTO {

    private Long UserId;

    private Long commentId;
}
