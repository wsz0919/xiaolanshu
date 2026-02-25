package com.wsz.xiaolanshu.notice.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserMqDTO {

    /**
     * 当前操作的用户 ID (发起关注的人，即 senderId)
     */
    private Long userId;

    /**
     * 被关注的用户 ID (接收通知的人，即 receiverId)
     */
    private Long followUserId;

    private Long unfollowUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}