package com.wsz.xiaolanshu.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 15:20
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum LikeUnlikeCommentTypeEnum {
    // 点赞
    LIKE(1),
    // 取消点赞
    UNLIKE(0),
    ;

    private final Integer code;

}
