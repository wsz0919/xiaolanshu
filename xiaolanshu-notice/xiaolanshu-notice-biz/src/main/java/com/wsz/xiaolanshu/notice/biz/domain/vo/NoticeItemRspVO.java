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

    @Data
    public static class NoticeUserVO {
        private String nickname;
        private String avatar;
        private Boolean isAuthor; // 是否为原笔记作者
    }
}
