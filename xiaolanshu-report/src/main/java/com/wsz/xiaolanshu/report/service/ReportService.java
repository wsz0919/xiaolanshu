package com.wsz.xiaolanshu.report.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import com.wsz.xiaolanshu.report.domain.vo.FindReportPageListReqVO;
import com.wsz.xiaolanshu.report.domain.vo.ProcessReportReqVO;
import com.wsz.xiaolanshu.report.domain.vo.SubmitReportReqVO;


/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:43
 * @Company:
 */
public interface ReportService {

    // C端提交举报
    Response<?> submitReport(SubmitReportReqVO reqVO);

    // B端处理举报
    Response<?> processReport(ProcessReportReqVO reqVO);

    // 分页查询举报列表
    PageResponse<ReportDO> findReportPageList(FindReportPageListReqVO reqVO);
}
