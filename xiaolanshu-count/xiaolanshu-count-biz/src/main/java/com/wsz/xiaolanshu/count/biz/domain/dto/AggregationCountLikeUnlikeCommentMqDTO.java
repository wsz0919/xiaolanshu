package com.wsz.xiaolanshu.count.biz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 15:43
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregationCountLikeUnlikeCommentMqDTO {

    /**
     * 评论 ID
     */
    private Long commentId;

    /**
     * 聚合后的计数
     */
    private Integer count;

}
