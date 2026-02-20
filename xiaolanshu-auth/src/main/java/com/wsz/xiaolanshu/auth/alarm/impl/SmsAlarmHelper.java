package com.wsz.xiaolanshu.auth.alarm.impl;

import com.wsz.xiaolanshu.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-25 22:41
 * @Company:
 */
@Slf4j
public class SmsAlarmHelper implements AlarmInterface {

    /**
     * 发送告警信息
     *
     * @param message
     * @return
     */
    @Override
    public boolean send(String message) {
        log.info("==> 【短信告警】：{}", message);

        return false;
    }

}
