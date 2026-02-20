package com.wsz.xiaolanshu.count.dto;

import lombok.*;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:33
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteCountByIdRspDTO {

    /**
     * 笔记 ID
     */
    private Long noteId;

    /**
     * 点赞数
     */
    private Long likeTotal;

    /**
     * 收藏数
     */
    private Long collectTotal;

    /**
     * 评论数
     */
    private Long commentTotal;

}
