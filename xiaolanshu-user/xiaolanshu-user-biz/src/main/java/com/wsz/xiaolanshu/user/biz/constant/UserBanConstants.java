package com.wsz.xiaolanshu.user.biz.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-19 22:22
 * @Company:
 */
public class UserBanConstants {

    public static final int NORMAL = 0;                 // 0: 正常
    public static final int GLOBAL_BAN = 1;             // 1: 全局封禁(踢出并禁止登录)
    public static final int BAN_PUBLISH_NOTE = 2;       // 2: 禁止发布/编辑笔记
    public static final int BAN_PUBLISH_COMMENT = 4;    // 4: 禁止发布评论
    public static final int BAN_LIKE_COLLECT = 8;       // 8: 禁止点赞/收藏
    public static final int BAN_UPDATE_PROFILE = 16;    // 16: 禁止修改资料
    public static final int BAN_PROFILE_ACCESS = 32;    // 32: 限制他人访问主页
    public static final int BAN_SEARCH = 64;            // 64: 禁止被搜索到
}
