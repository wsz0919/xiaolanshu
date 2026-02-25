package com.wsz.xiaolanshu.notice.biz.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:23
 * @Company:
 */
public interface MQConstants {

    /**
     * Topic: 关注、取关共用一个
     */
    String TOPIC_FOLLOW_OR_UNFOLLOW = "FollowUnfollowTopic";

    /**
     * 关注标签
     */
    String TAG_FOLLOW = "Follow";

    /**
     * 关注标签
     */
    String TAG_UNFOLLOW = "Unfollow";

    /**
     * Topic: 点赞、取消点赞共用一个
     */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";

    /**
     * Topic: 评论发布
     */
    String TOPIC_PUBLISH_COMMENT = "PublishCommentTopic";

    /**
     * Topic: 收藏、取消收藏共用一个
     */
    String TOPIC_COLLECT_OR_UN_COLLECT = "CollectUnCollectTopic";

    /**
     * Topic: 评论点赞、取消点赞共用一个 Topic
     */
    String TOPIC_COMMENT_LIKE_OR_UNLIKE = "CommentLikeUnlikeTopic";

}
