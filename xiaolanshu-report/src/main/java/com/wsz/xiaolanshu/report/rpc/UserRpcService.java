package com.wsz.xiaolanshu.report.rpc;

import com.wsz.xiaolanshu.user.api.UserFeignApi;
import com.wsz.xiaolanshu.user.dto.req.AdminUpdateUserStatusReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-21 19:35
 * @Company:
 */
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    public void banUser(Long targetUserId, Integer status) {
        AdminUpdateUserStatusReqVO vo = new AdminUpdateUserStatusReqVO();
        vo.setId(targetUserId);
        vo.setStatus(status);

        userFeignApi.banUser(vo);
    }
}
