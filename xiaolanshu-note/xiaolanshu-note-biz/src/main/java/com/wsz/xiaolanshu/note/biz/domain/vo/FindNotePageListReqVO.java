package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 14:46
 * @Company:
 */
@Data
public class FindNotePageListReqVO {
    private Long pageNo = 1L; // 默认第一页
    private Long pageSize = 10L; // 默认每页10条

    // 搜索条件
    private String title; // 笔记标题模糊搜索
    private Long creatorId; // 创作者ID
    private Integer status; // 笔记状态 (例如：0-待审核 1-正常 2-违规下架)
}