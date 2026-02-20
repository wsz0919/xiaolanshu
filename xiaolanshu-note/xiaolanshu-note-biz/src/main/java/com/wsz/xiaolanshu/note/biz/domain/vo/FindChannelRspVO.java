package com.wsz.xiaolanshu.note.biz.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 22:26
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindChannelRspVO {

    /**
     * 频道 ID
     */
    private Long id;

    /**
     * 频道名称
     */
    private String name;

}
