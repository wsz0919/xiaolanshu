package com.wsz.xiaolanshu.note.biz.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-10 21:22
 * @Company:
 */
public class RedisConstants {

    /**
     * 笔记详情 KEY 前缀
     */
    public static final String NOTE_DETAIL_KEY = "note:detail:";

    /**
     * 布隆过滤器：用户笔记点赞
     */
    public static final String BLOOM_USER_NOTE_LIKE_LIST_KEY = "bloom:note:likes:";

    /**
     * 用户笔记点赞列表 ZSet 前缀
     */
    public static final String USER_NOTE_LIKE_ZSET_KEY = "user:note:likes:";

    /**
     * 布隆过滤器：用户笔记收藏 前缀
     */
    public static final String BLOOM_USER_NOTE_COLLECT_LIST_KEY = "bloom:note:collects:";

    /**
     * 用户笔记收藏列表 ZSet 前缀
     */
    public static final String USER_NOTE_COLLECT_ZSET_KEY = "user:note:collects:";

    /**
     * Roaring Bitmap：用户笔记点赞 前缀
     */
    public static final String R_BITMAP_USER_NOTE_LIKE_LIST_KEY = "rbitmap:note:likes:";

    /**
     * Roaring Bitmap：用户笔记收藏 前缀
     */
    public static final String R_BITMAP_USER_NOTE_COLLECT_LIST_KEY = "rbitmap:note:collects:";

    /**
     * 已发布笔记列表 KEY 前缀
     */
    private static final String PUBLISHED_NOTE_LIST_KEY = "note:published:list:";

    /**
     * 笔记计数 KEY 前缀
     */
    public static final String NOTE_COUNT_KEY = "count:note:";

    /**
     * Hash Field: 点赞总数
     */
    public static final String FIELD_LIKE_TOTAL = "likeTotal";

    /**
     * Hash Field: 收藏总数
     */
    public static final String FIELD_COLLECT_TOTAL = "collectTotal";

    /**
     * Hash Field: 评论总数
     */
    public static final String FIELD_COMMENT_TOTAL = "commentTotal";

    /**
     * 封面模版列表 KEY
     */
    public static final String COVER_TEMPLATE_LIST_KEY = "cover:templates";

    /**
     * 封面模版详情 KEY 前缀
     */
    public static final String COVER_TEMPLATE_DETAIL_KEY = "cover:template:";

    /**
     * 构建封面模版详情 KEY
     */
    public static String buildCoverTemplateDetailKey(Long templateId) {
        return COVER_TEMPLATE_DETAIL_KEY + templateId;
    }

    /**
     * 构建完整的笔记计数 KEY
     * @param noteId
     * @return
     */
    public static String buildNoteCountKey(Long noteId) {
        return NOTE_COUNT_KEY + noteId;
    }

    /**
     * 构建完整的已发布笔记列表 KEY
     * @param userId
     * @return
     */
    public static String buildPublishedNoteListKey(Long userId) {
        return PUBLISHED_NOTE_LIST_KEY + userId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记收藏 KEY
     * @param userId
     * @return
     */
    public static String buildRBitmapUserNoteCollectListKey(Long userId) {
        return R_BITMAP_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记点赞 KEY
     * @param userId
     * @return
     */
    public static String buildRBitmapUserNoteLikeListKey(Long userId) {
        return R_BITMAP_USER_NOTE_LIKE_LIST_KEY + userId;
    }

    /**
     * 构建完整的笔记详情 KEY
     * @param noteId
     * @return
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }

    /**
     * 构建完整的布隆过滤器：用户笔记点赞 KEY
     * @param userId
     * @return
     */
    public static String buildBloomUserNoteLikeListKey(Long userId) {
        return BLOOM_USER_NOTE_LIKE_LIST_KEY + userId;
    }

    /**
     * 构建完整的用户笔记点赞列表 ZSet KEY
     * @param userId
     * @return
     */
    public static String buildUserNoteLikeZSetKey(Long userId) {
        return USER_NOTE_LIKE_ZSET_KEY + userId;
    }

    /**
     * 构建完整的布隆过滤器：用户笔记收藏 KEY
     * @param userId
     * @return
     */
    public static String buildBloomUserNoteCollectListKey(Long userId) {
        return BLOOM_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

    /**
     * 构建完整的用户笔记收藏列表 ZSet KEY
     * @param userId
     * @return
     */
    public static String buildUserNoteCollectZSetKey(Long userId) {
        return USER_NOTE_COLLECT_ZSET_KEY + userId;
    }
}
