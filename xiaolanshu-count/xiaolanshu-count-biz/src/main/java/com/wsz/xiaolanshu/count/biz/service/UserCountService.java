package com.wsz.xiaolanshu.count.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdReqDTO;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdRspDTO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 20:51
 * @Company:
 */
public interface UserCountService {

    /**
     * 查询用户相关计数
     * @param findUserCountsByIdReqDTO
     * @return
     */
    Response<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

}
