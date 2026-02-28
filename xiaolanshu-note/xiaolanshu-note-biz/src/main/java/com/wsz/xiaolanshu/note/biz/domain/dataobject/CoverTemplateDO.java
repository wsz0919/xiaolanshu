package com.wsz.xiaolanshu.note.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-28 22:04
 * @Company:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverTemplateDO {
    private Long id;
    private String name;
    private String imgUrl;
    private Integer fontSize;
    private String fontColor;
    private Integer weekX;
    private Integer weekY;
    private Integer dateX; // -1 表示右对齐
    private Integer dateY;
    private Integer titleY;
}
