package com.wsz.xiaolanshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-01-29 17:24
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum NoteOperateEnum {
    // 笔记发布
    PUBLISH(1),
    // 笔记删除
    DELETE(0),
    ;

    private final Integer code;

}
