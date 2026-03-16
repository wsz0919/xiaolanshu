package com.wsz.xiaolanshu.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 9:48
 * @Company:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindUserRoleAndPermissionRspDTO {
    private List<String> roles;
    private List<String> permissions;
}
