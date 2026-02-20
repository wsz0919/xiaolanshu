package com.wsz.xiaolanshu.count.api;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.constant.ApiConstants;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdRspDTO;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdReqDTO;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdReqDTO;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 15:28
 * @Company:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeignApi {

    String PREFIX = "/count";

    /**
     * 查询用户计数
     *
     * @param findUserCountsByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/user/data")
    Response<FindUserCountsByIdRspDTO> findUserCount(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

    /**
     * 查询笔记计数
     *
     * @param findNoteCountByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/note/data")
    Response<FindNoteCountByIdRspDTO> findNoteCount(@RequestBody FindNoteCountByIdReqDTO findNoteCountByIdReqDTO);

}
