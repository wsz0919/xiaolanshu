package com.wsz.xiaolanshu.report.enums;

import com.wsz.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 19:59
 * @Company:
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("REPORT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("REPORT-10001", "参数错误"),
    // ----------- 业务异常状态码 -----------
    PROCESS_REPORT_FAIL("REPORT-10002", "违规处罚执行失败，请检查相关服务运行状态"),
    REPORT_DO_NOT_EXIST("REPORT-10003", "举报记录不存在"),
    REPORT_IS_PROCESS("REPORT-10004", "该举报已被处理，请勿重复操作"),
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
