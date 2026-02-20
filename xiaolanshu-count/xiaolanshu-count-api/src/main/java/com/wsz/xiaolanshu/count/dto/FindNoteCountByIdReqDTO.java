package com.wsz.xiaolanshu.count.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:33
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteCountByIdReqDTO {

    /**
     * 笔记 ID
     */
    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

}
