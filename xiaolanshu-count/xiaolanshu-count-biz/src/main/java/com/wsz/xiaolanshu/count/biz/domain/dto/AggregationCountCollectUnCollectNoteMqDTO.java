package com.wsz.xiaolanshu.count.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-01-29 17:11
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregationCountCollectUnCollectNoteMqDTO {
    /**
     * 笔记发布者 ID
     */
    private Long creatorId;

    /**
     * 笔记 ID
     */
    private Long noteId;

    /**
     * 聚合后的计数
     */
    private Integer count;
}
