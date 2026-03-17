package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员分页查询笔记响应
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 14:00
 * @Company:
 */
@Data
public class AdminNotePageRspVO {
    /**
     * 总记录数
     */
    private Long total;

    /**
     * 笔记列表
     */
    private List<AdminNoteItemRspVO> list;

    @Data
    public static class AdminNoteItemRspVO {
        /**
         * 笔记ID
         */
        private Long id;

        /**
         * 标题
         */
        private String title;

        /**
         * 发布者ID
         */
        private Long creatorId;

        /**
         * 发布时间
         */
        private LocalDateTime createTime;

        /**
         * 状态
         */
        private Integer status;
    }
}