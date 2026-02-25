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
public class NoticeNoteLikeMqDTO {

    /**
     * 点赞/取消点赞的用户 ID (即 senderId)
     */
    private Long userId;

    /**
     * 被点赞的笔记 ID
     */
    private Long noteId;

    /**
     * 操作类型：1-点赞，0-取消点赞
     */
    private Integer type;

    /**
     * 笔记的作者 ID (接收通知的人，即 receiverId)
     * 注意：如果报 receiver_id cannot be null，就是因为发送方没传这个字段！
     */
    private Long noteCreatorId;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}