package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 14:46
 * @Company:
 */
@Data
@Builder
public class AdminNoteRspVO {
    private Long id;
    private String title;
    private Long creatorId;
    private Integer type; // 图文 or 视频
    private String cover;
    private String nickName;
    private String content;
    private Integer status;
    private Integer visible;
    private LocalDateTime createTime;
}