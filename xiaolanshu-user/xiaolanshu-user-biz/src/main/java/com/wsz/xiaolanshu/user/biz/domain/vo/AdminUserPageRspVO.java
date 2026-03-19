package com.wsz.xiaolanshu.user.biz.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 15:31
 * @Company:
 */
@Data
public class AdminUserPageRspVO {
    private Long id;
    private String xiaolanshuId;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer sex;
    private Integer status;
    private LocalDateTime createTime;
}