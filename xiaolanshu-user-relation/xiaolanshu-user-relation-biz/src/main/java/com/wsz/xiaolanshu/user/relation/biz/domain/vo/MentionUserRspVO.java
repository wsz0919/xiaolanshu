package com.wsz.xiaolanshu.user.relation.biz.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-27 19:40
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MentionUserRspVO {
    private Long userId;
    private String nickname;
    private String avatar;
}
