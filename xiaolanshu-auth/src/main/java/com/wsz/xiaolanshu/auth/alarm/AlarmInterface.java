package com.wsz.xiaolanshu.auth.alarm;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-25 22:40
 * @Company:
 */
public interface AlarmInterface {

    /**
     * 发送告警信息
     *
     * @param message
     * @return
     */
    boolean send(String message);
}
