package com.wsz.xiaolanshu.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 21:15
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentReqDTO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

    @NotBlank(message = "年月不能为空")
    private String yearMonth; // 格式如 "2023-10"，需要调用方通过评论的 create_time 解析出来

    @NotBlank(message = "评论内容 UUID 不能为空")
    private String contentUuid;

}
