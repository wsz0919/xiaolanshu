package com.wsz.xiaolanshu.notice.biz.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:51
 * @Company:
 */
@Data
public class NoticePublishCommentMqDTO {
    private Long noteId;
    private Long commentId;
    private Long replyCommentId;
    private Long replyUserId; // 接收我们刚刚加上的字段
    private Long creatorId;

    /**
     * 接收 @ 用户集合
     */
    private List<Long> mentionUserIds;
}
