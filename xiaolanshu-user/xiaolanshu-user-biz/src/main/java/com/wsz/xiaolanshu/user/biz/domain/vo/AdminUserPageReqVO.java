package com.wsz.xiaolanshu.user.biz.domain.vo;

import lombok.Data;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 15:30
 * @Company:
 */
@Data
public class AdminUserPageReqVO {
    private Long pageNo = 1L;
    private Long pageSize = 10L;

    // 搜索条件
    private String phone; // 手机号模糊搜索
    private String xiaolanshuId; // 小蓝书号精确/模糊搜索
    private Integer status; // 用户状态 (例如：0-正常 1-封禁)
}
