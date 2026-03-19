package com.wsz.xiaolanshu.kv.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-19 19:52
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchFindNoteContentReqDTO {

    /**
     * 笔记内容 UUID 集合
     */
    @NotEmpty(message = "笔记内容 UUID 集合不能为空")
    private List<String> uuids;
}
