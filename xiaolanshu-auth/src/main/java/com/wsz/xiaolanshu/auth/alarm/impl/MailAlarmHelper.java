package com.wsz.xiaolanshu.auth.alarm.impl;

import com.wsz.xiaolanshu.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-25 22:40
 * @Company:
 */
@Slf4j
public class MailAlarmHelper implements AlarmInterface {

    /**
     * 发送告警信息
     *
     * @param message
     * @return
     */
    @Override
    public boolean send(String message) {
        log.info("==> 【邮件告警】：{}", message);

        return false;
    }
}
