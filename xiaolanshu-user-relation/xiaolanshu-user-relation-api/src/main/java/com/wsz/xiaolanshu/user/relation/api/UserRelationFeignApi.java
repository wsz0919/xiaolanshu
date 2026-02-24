package com.wsz.xiaolanshu.user.relation.api;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.relation.constant.ApiConstants;
import com.wsz.xiaolanshu.user.relation.dto.FollowUserReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 19:21
 * @Company:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserRelationFeignApi {

    String PREFIX = "/relation";

    @PostMapping(PREFIX + "/checkFollowRelation")
    Response<?> isFollowOrUnfollow(@RequestBody FollowUserReqDTO followUserReqDTO);
}
