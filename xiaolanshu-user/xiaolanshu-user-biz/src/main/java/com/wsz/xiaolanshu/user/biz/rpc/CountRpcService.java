package com.wsz.xiaolanshu.user.biz.rpc;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.api.CountFeignApi;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdReqDTO;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 15:32
 * @Company:
 */
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 查询用户计数信息
     * @param userId
     * @return
     */
    public FindUserCountsByIdRspDTO findUserCountById(Long userId) {
        FindUserCountsByIdReqDTO findUserCountsByIdReqDTO = new FindUserCountsByIdReqDTO();
        findUserCountsByIdReqDTO.setUserId(userId);

        Response<FindUserCountsByIdRspDTO> response = countFeignApi.findUserCount(findUserCountsByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

}
