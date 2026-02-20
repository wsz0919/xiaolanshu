package com.wsz.xiaolanshu.user.relation.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-01-28 15:22
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    // 关注
    FOLLOW(1),
    // 取关
    UNFOLLOW(0),
    ;

    private final Integer code;

}
