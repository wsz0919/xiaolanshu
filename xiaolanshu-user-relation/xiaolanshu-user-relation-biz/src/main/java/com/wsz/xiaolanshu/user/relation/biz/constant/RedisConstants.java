package com.wsz.xiaolanshu.user.relation.biz.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-16 17:21
 * @Company:
 */
public class RedisConstants {

    /**
     * 关注列表 KEY 前缀
     */
    private static final String USER_FOLLOWING_KEY_PREFIX = "following:";

    /**
     * 粉丝列表 KEY 前缀
     */
    private static final String USER_FANS_KEY_PREFIX = "fans:";

    /**
     * 提到的用户列表缓存 KEY 前缀
     */
    private static final String MENTION_LIST_KEY_PREFIX = "mention:list:";

    /**
     * 构建屠刀的用户列表完整的 KEY
     * @param userId
     * @return
     */
    public static String buildMentionListKey(Long userId) {
        return MENTION_LIST_KEY_PREFIX + userId;
    }

    /**
     * 构建关注列表完整的 KEY
     * @param userId
     * @return
     */
    public static String buildUserFollowingKey(Long userId) {
        return USER_FOLLOWING_KEY_PREFIX + userId;
    }

    /**
     * 构建粉丝列表完整的 KEY
     * @param userId
     * @return
     */
    public static String buildUserFansKey(Long userId) {
        return USER_FANS_KEY_PREFIX + userId;
    }
}
