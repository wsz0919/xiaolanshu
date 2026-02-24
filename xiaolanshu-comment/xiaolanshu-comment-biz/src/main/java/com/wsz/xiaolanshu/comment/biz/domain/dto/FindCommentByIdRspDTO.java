package com.wsz.xiaolanshu.comment.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 17:26
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentByIdRspDTO {

    /**
     * 评论 ID (对应 t_comment.id)
     */
    private Long id;

    /**
     * 关联的笔记 ID (对应 t_comment.note_id)
     * 作用：Notice服务需要拿它去查询笔记的封面图，以及判断评论人是不是笔记作者。
     */
    private Long noteId;

    /**
     * 发布该评论的用户 ID (对应 t_comment.user_id)
     */
    private Long userId;

    /**
     * 评论内容 UUID (对应 t_comment.content_uuid)
     * 作用：Notice服务需要拿这个 UUID 去调 KV 服务，查出评论的真实长文本内容。
     */
    private String contentUuid;

    /**
     * 回复的哪个评论的 ID (对应 t_comment.reply_comment_id)
     * 作用：如果是回复别人的评论(sub_type=32)，Notice服务需要根据它去查出被回复的那条原评论的内容(quoteText)。
     */
    private Long replyCommentId;

    /**
     * 评论附加图片 URL (对应 t_comment.image_url)
     * 作用：如果评论带图，可以作为前端的右侧展示图。
     */
    private String imageUrl;

    /**
     * 级别 (对应 t_comment.level) 1:一级 2:二级
     */
    private Integer level;

    /**
     * 创建时间 (对应 t_comment.create_time)
     */
    private LocalDateTime createTime;

}
