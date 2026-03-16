package com.wsz.xiaolanshu.report.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import com.wsz.xiaolanshu.report.domain.vo.ProcessReportReqVO;
import com.wsz.xiaolanshu.report.domain.vo.SubmitReportReqVO;
import com.wsz.xiaolanshu.report.mapper.ReportDOMapper;
import com.wsz.xiaolanshu.report.service.ReportService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportDOMapper reportDOMapper;

    @Override
    public Response<?> submitReport(SubmitReportReqVO reqVO) {
        // 获取当前登录用户ID (举报人)
        Long reporterId = StpUtil.getLoginIdAsLong();

        // 构建实体
        ReportDO reportDO = ReportDO.builder()
                .reporterId(reporterId)
                .targetType(reqVO.getTargetType())
                .targetId(reqVO.getTargetId())
                .reportReason(reqVO.getReportReason())
                .reportDetail(reqVO.getReportDetail())
                .images(reqVO.getImages())
                .build();

        // 写入数据库
        reportDOMapper.insert(reportDO);

        return Response.success("举报提交成功，感谢您的反馈！");
    }

    @Override
    public Response<?> processReport(ProcessReportReqVO reqVO) {
        // 获取当前操作的管理员ID
        Long processorId = StpUtil.getLoginIdAsLong();

        // 更新举报记录状态
        reportDOMapper.updateStatus(reqVO.getReportId(), reqVO.getStatus(), processorId, reqVO.getProcessRemark());

        /*
         * TODO 进阶逻辑：
         * 如果 reqVO.getStatus() == 1 (判定违规)
         * 这里可以进一步根据 targetType，通过 RPC 调用对应的服务自动执行封号/下架逻辑。
         * 例如：
         * if (targetType == 2) noteRpcService.offlineNote(targetId);
         */

        return Response.success("举报处理成功！");
    }
}
