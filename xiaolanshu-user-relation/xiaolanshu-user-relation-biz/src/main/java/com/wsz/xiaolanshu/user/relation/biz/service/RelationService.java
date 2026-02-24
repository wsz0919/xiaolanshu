package com.wsz.xiaolanshu.user.relation.biz.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.relation.biz.domain.dto.FollowUserReqDTO;
import com.wsz.xiaolanshu.user.relation.biz.domain.vo.*;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-16 16:54
 * @Company:
 */
public interface RelationService {

    /**
     * 关注用户
     * @param followUserReqVO
     * @return
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);

    /**
     * 取关用户
     * @param unfollowUserReqVO
     * @return
     */
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);

    /**
     * 查询关注列表
     * @param findFollowingListReqVO
     * @return
     */
    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    /**
     * 查询粉丝列表
     * @param findFansListReqVO
     * @return
     */
    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);

    /**
     * 是否关注笔记作者
     * @param followUserReqVO
     * @return
     */
    Response<?> checkFollowStatus(FollowUserReqVO followUserReqVO);

    /**
     * 通知当前用户和关注自己用户的关系
     * @param followUserReqDTO
     * @return
     */
    Response<?> checkFollowStatus(FollowUserReqDTO followUserReqDTO);
}
