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
public class NoticeNoteCollectMqDTO {

    /**
     * 收藏/取消收藏的用户 ID (即 senderId)
     */
    private Long userId;

    /**
     * 被收藏的笔记 ID
     */
    private Long noteId;

    /**
     * 操作类型：1-收藏，0-取消收藏
     */
    private Integer type;

    /**
     * 笔记的作者 ID (接收通知的人，即 receiverId)
     */
    private Long noteCreatorId;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}