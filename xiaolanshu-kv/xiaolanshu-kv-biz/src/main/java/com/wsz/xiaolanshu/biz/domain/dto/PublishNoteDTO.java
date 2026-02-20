package com.wsz.xiaolanshu.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 17:42
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishNoteDTO {
    private Long id;

    private String title;

    private Boolean isContentEmpty;

    private Long creatorId;

    private Long topicId;

    private String topicName;

    private Boolean isTop;

    private Integer type;

    private String imgUris;

    private String videoUri;

    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer status;

    private String contentUuid;

    private String content;

    private Long channelId;

    private String topicIds;
}
