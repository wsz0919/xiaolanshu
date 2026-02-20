package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-10 21:15
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteDetailRspVO {

    private Long id;

    private Integer type;

    private String title;

    private String content;

    private List<String> imgUris;

    /**
     * 话题集合
     */
    List<FindTopicRspVO> topics;

    private Long creatorId;

    private String creatorName;

    private String avatar;

    private String videoUri;

    /**
     * 编辑时间
     */
    private String updateTime;

    /**
     * 是否可见
     */
    private Integer visible;

    /**
     * 被点赞数
     */
    private String likeTotal;

    /**
     * 被收藏数
     */
    private String collectTotal;

    /**
     * 被评论数
     */
    private String commentTotal;

}
