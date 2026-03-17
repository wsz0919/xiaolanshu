package com.wsz.xiaolanshu.report.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import com.wsz.xiaolanshu.report.domain.vo.FindReportPageListReqVO;
import com.wsz.xiaolanshu.report.domain.vo.ProcessReportReqVO;
import com.wsz.xiaolanshu.report.domain.vo.SubmitReportReqVO;
import com.wsz.xiaolanshu.report.mapper.ReportDOMapper;
import com.wsz.xiaolanshu.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportDOMapper reportDOMapper;

    @Override
    public Response<?> submitReport(SubmitReportReqVO reqVO) {
        Long reporterId = StpUtil.getLoginIdAsLong();

        ReportDO reportDO = ReportDO.builder()
                .reporterId(reporterId)
                .targetType(reqVO.getTargetType())
                .targetId(reqVO.getTargetId())
                .reportReason(reqVO.getReportReason())
                .reportDetail(reqVO.getReportDetail())
                .images(reqVO.getImages())
                .status(0) // 0: 待处理
                .build();

        reportDOMapper.insertSelective(reportDO);
        return Response.success("举报已提交，我们会尽快核实处理！");
    }

    @Override
    public Response<?> processReport(ProcessReportReqVO reqVO) {
        Long processorId = StpUtil.getLoginIdAsLong();

        ReportDO updateDO = new ReportDO();
        updateDO.setId(reqVO.getReportId());
        updateDO.setStatus(reqVO.getStatus()); // 1:违规 2:驳回
        updateDO.setProcessRemark(reqVO.getProcessRemark());
        updateDO.setProcessorId(processorId);

        reportDOMapper.updateByPrimaryKeySelective(updateDO);

        // 如果被判定为违规 (status == 1)，你可以后续通过 RPC 调用 user-api 或 note-api 来下架笔记或封号
        if (reqVO.getStatus() == 1) {
            log.info("==> 举报 [ID:{}] 判定违规，准备执行相应处罚逻辑...", reqVO.getReportId());
            // TODO: rpc call to user/note module
        }

        return Response.success("处理成功");
    }

    @Override
    public PageResponse<ReportDO> findReportPageList(FindReportPageListReqVO reqVO) {
        long total = reportDOMapper.selectCount(reqVO.getStatus());
        Integer pageNo = reqVO.getPageNo();
        Integer pageSize = reqVO.getPageSize();
        if (total == 0) {
            return PageResponse.success(null, 0L, 0);
        }

        int offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        List<ReportDO> list = reportDOMapper.selectPageList(offset, reqVO.getPageSize(), reqVO.getStatus());

        return PageResponse.success(list, pageNo, total, pageSize);
    }
}
