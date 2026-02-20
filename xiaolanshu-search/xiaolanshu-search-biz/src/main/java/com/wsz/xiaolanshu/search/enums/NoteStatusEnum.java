package com.wsz.xiaolanshu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-06 14:53
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum NoteStatusEnum {

    BE_EXAMINE(0), // 待审核
    NORMAL(1), // 正常展示
    DELETED(2), // 被删除
    DOWNED(3), // 被下架
    ;

    private final Integer code;

}
