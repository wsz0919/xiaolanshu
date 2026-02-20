package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:55
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindProfileNoteRspVO {

    /**
     * 笔记 ID
     */
    private String id;

    /**
     * 笔记类型
     */
    private Integer type;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 视频连接
     */
    private String videoUri;

    /**
     * 标题
     */
    private String title;

    /**
     * 发布者用户 ID
     */
    private Long creatorId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 被点赞量
     */
    private String likeTotal;

}
