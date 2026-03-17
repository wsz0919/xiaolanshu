package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.Data;

/**
 * 管理员分页查询笔记请求参数
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 14:00
 * @Company:
 */
@Data
public class AdminNotePageReqVO {
    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 笔记状态
     */
    private Integer status;

    /**
     * 发布者ID
     */
    private Long creatorId;
}