package com.wsz.xiaolanshu.comment.api;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.comment.constant.ApiConstants;
import com.wsz.xiaolanshu.comment.dto.FindCommentByIdRspDTO;
import com.wsz.xiaolanshu.comment.dto.LikeCommentReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 17:07
 * @Company:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CommentFeignApi {

    String PREFIX = "/comment";

    @PostMapping(PREFIX + "/getNoteIdByCommentId")
    Response<FindCommentByIdRspDTO> getNoteIdByCommentId(@RequestBody LikeCommentReqDTO vo);
}
