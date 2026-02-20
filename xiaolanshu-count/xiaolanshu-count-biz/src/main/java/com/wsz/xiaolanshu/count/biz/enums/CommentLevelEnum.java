package com.wsz.xiaolanshu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-09 13:52
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum CommentLevelEnum {
    // 一级评论
    ONE(1),
    // 二级评论
    TWO(2),
    ;

    private final Integer code;

}
