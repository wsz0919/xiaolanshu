package com.wsz.xiaolanshu.user.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.domain.vo.FindUserProfileReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.FindUserProfileRspVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.FindUserTopReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.UpdateUserInfoReqVO;
import com.wsz.xiaolanshu.user.dto.req.*;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByIdRspDTO;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByPhoneRspDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 21:39
 * @Company:
 */
public interface UserService {
    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneReqDTO
     * @return
     */
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 更新密码
     *
     * @param updateUserPasswordReqDTO
     * @return
     */
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param findUserByIdReqDTO
     * @return
     */
    Response<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO);

    /**
     * 批量根据用户 ID 查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    Response<List<FindUserByIdRspDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO);

    /**
     * 获取用户主页信息
     *
     * @return
     */
    Response<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO);

    /**
     * 查询最新注册/活跃的用户
     */
    Response<List<FindUserByIdRspDTO>> findTopUsers(FindUserTopReqVO vo);

    Response<List<String>> getUserRoleKeys(@RequestParam("userId") Long userId);

    Response<?> banUser(@RequestBody Map<String, Object> params);
}
