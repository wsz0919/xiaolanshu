package com.wsz.xiaolanshu.kv.dto.req;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 15:19
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchAddCommentContentReqDTO {

    @NotEmpty(message = "评论内容集合不能为空")
    @Valid  // 指定集合内的评论 DTO，也需要进行参数校验
    private List<CommentContentReqDTO> comments;

}