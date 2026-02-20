package com.wsz.xiaolanshu.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 16:38
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

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static CommentLevelEnum valueOf(Integer code) {
        for (CommentLevelEnum commentLevelEnum : CommentLevelEnum.values()) {
            if (Objects.equals(code, commentLevelEnum.getCode())) {
                return commentLevelEnum;
            }
        }
        return null;
    }

}
