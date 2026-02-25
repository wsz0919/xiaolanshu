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
public class NoticeCommentLikeMqDTO {

    /**
     * 点赞/取消点赞的用户 ID (即 senderId)
     */
    private Long userId;

    /**
     * 被点赞的评论 ID
     */
    private Long commentId;

    /**
     * 操作类型：1-点赞，0-取消点赞
     */
    private Integer type;

    /**
     * 评论的发布者 ID (接收通知的人，即 receiverId)
     */
    private Long commentCreatorId;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}