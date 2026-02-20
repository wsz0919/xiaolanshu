package com.wsz.xiaolanshu.data.align.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-02 15:24
 * @Company:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectUnCollectNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消收藏， 1：收藏
     */
    private Integer type;

    private LocalDateTime createTime;

    /**
     * 笔记发布者 ID
     */
    private Long noteCreatorId;
}
