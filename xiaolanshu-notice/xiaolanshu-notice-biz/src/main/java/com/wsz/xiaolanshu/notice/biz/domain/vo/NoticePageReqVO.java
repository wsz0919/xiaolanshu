package com.wsz.xiaolanshu.notice.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:00
 * @Company:
 */
@Data
public class NoticePageReqVO {
    @NotNull(message = "Tab类型不能为空")
    private String tabId; // 前端传的 'comment', 'like_collect', 'follow'
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
