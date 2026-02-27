package com.wsz.xiaolanshu.notice.biz.domain.vo;

import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:01
 * @Company:
 */
@Data
public class NoticeItemRspVO {
    private String id;
    private String type; // 'like', 'reply', 'follow'
    private NoticeUserVO user;
    private String actionText; // "回复了你的评论"
    private String time; // "10分钟前"
    private String content; // 评论正文
    private String quoteText; // 被引用的原文 / 笔记标题
    private String cover; // 右侧封面图
    private Boolean isMutual; // 是否互粉 (仅关注Tab)
    private Integer subType; // 细分动作
    private Long targetId; // 笔记 ID 或 评论 ID
    private Long noteId; // 笔记 ID
    private Long currentId; // 当前用户 ID

    @Data
    public static class NoticeUserVO {
        private Long userId; // 列表用户 ID
        private String nickname;
        private String avatar;
        private Boolean isAuthor; // 是否为原笔记作者
    }
}
