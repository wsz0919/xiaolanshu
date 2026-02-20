package com.wsz.xiaolanshu.user.relation.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-16 16:53
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserReqVO {

    @NotNull(message = "被关注用户 ID 不能为空")
    private Long followUserId;
}
