package com.wsz.xiaolanshu.report.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.biz.context.holder.LoginUserContextHolder;
import com.wsz.framework.common.exception.BizException;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import com.wsz.xiaolanshu.report.domain.vo.FindReportPageListReqVO;
import com.wsz.xiaolanshu.report.domain.vo.ProcessReportReqVO;
import com.wsz.xiaolanshu.report.domain.vo.SubmitReportReqVO;
import com.wsz.xiaolanshu.report.enums.ResponseCodeEnum;
import com.wsz.xiaolanshu.report.mapper.ReportDOMapper;
import com.wsz.xiaolanshu.report.rpc.CommentRpcService;
import com.wsz.xiaolanshu.report.rpc.NoteRpcService;
import com.wsz.xiaolanshu.report.rpc.UserRpcService;
import com.wsz.xiaolanshu.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportDOMapper reportDOMapper;

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private NoteRpcService noteRpcService;

    @Resource
    private CommentRpcService commentRpcService;

    @Override
    public Response<?> submitReport(SubmitReportReqVO reqVO) {
        Long reporterId = LoginUserContextHolder.getUserId();

        long count = reportDOMapper.selectByReporterAndTarget(reporterId, reqVO.getTargetType(), reqVO.getTargetId());

        if (count > 0) {
            return Response.success("薯队长已收到，无需重复操作哦");
        }

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
    @Transactional(rollbackFor = Exception.class)
    public Response<?> processReport(ProcessReportReqVO reqVO) {
        Long processorId = LoginUserContextHolder.getUserId();

        // 1. 先查询出举报详情，为了拿到 TargetType 和 TargetId
        ReportDO reportDO = reportDOMapper.selectByPrimaryKey(reqVO.getReportId());
        if (reportDO == null) {
            throw new BizException(ResponseCodeEnum.REPORT_DO_NOT_EXIST);
        }
        if (reportDO.getStatus() != 0) {
            throw new BizException(ResponseCodeEnum.REPORT_IS_PROCESS);
        }

        // 2. 更新数据库的处理状态
        ReportDO updateDO = new ReportDO();
        updateDO.setId(reqVO.getReportId());
        updateDO.setStatus(reqVO.getStatus()); // 1:违规 2:驳回
        updateDO.setProcessRemark(reqVO.getProcessRemark());
        updateDO.setProcessorId(processorId);
        reportDOMapper.updateByPrimaryKeySelective(updateDO);

        // 3. 如果判定为违规 (status == 1)，通过 RPC 执行相应的跨模块处罚
        if (reqVO.getStatus() == 1) {
            Long targetId = reportDO.getTargetId();
            Integer targetType = reportDO.getTargetType();

            log.info("==> 举报 [ID:{}] 判定违规，准备执行处罚逻辑。类型: {}, 目标ID: {}",
                    reqVO.getReportId(), targetType, targetId);

            try {
                switch (targetType) {
                    case 1:
                        // 类型1：举报用户 -> 跨服务调用：封禁用户
                        // 相当于调用 /user/admin/user/ban 接口
                        userRpcService.banUser(targetId, 0);
                        break;
                    case 2:
                        // 类型2：举报笔记 -> 跨服务调用：下架笔记
                        // 相当于调用 /note/admin/note/offline 接口
                        noteRpcService.offlineNote(targetId, 3);
                        break;
                    case 3:
                        // 类型3：举报评论 -> 跨服务调用：删除评论
                        commentRpcService.deleteComment(targetId);
                        break;
                    default:
                        log.warn("未知的举报类型: {}", targetType);
                }
            } catch (Exception e) {
                log.error("==> 举报处理处罚执行失败，跨服务调用异常", e);
                // 抛出异常，触发本地事务回滚，防止状态变成了“已违规”但实际上没封禁成功
                throw new BizException(ResponseCodeEnum.PROCESS_REPORT_FAIL);
            }
        }

        return Response.success("举报处理成功");
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
