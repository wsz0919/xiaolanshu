package com.wsz.xiaolanshu.kv.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-09 14:30
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentRspDTO {

    /**
     * 评论内容 UUID
     */
    private String contentId;

    /**
     * 评论内容
     */
    private String content;

}
