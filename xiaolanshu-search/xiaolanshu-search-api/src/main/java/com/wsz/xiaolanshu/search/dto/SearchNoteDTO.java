package com.wsz.xiaolanshu.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-09 14:13
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchNoteDTO {
    private Long noteId;
    private String title;
    private Integer type; // 笔记类型
    private String cover;
    private String videoUri;
    private Long creatorId; // 创作者ID
    private String nickname;
    private String avatar;
    private Integer likeTotal; // 点赞总数
}
