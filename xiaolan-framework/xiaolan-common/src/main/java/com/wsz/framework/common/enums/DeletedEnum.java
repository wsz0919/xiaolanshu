package com.wsz.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-25 20:15
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum DeletedEnum {
    YES(true),
    NO(false);

    private final Boolean value;
}
