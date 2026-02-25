package com.wsz.xiaolanshu.notice.biz.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-25 17:18
 * @Company:
 */
public class RedisConstants {

    /**
     * 用户通知列表 ZSet 的 Key 前缀
     * 格式: notice:zset:{userId}:{type}
     * type: 1-赞和收藏, 2-新增关注, 3-评论和@
     */
    public static final String NOTICE_ZSET_KEY_PREFIX = "notice:zset:";

    /**
     * 构建通知 ZSet Key
     */
    public static String buildNoticeZSetKey(Long userId, Integer type) {
        return NOTICE_ZSET_KEY_PREFIX + userId + ":" + type;
    }

}
