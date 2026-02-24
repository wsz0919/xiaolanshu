package com.wsz.xiaolanshu.user.relation.biz.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 19:12
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserReqDTO {

    @NotNull(message = "被关注用户 ID 不能为空")
    private Long receiverId;

    @NotNull(message = "发送关注请求的用户 ID 不能为空")
    private Long senderId;
}
