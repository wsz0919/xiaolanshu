package com.wsz.xiaolanshu.note.biz.domain.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:45
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindDiscoverNotePageListReqVO {

    /**
     * 频道 ID
     */
    private Long channelId;

    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;

}

