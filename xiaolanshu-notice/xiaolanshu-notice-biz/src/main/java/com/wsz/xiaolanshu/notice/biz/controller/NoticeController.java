package com.wsz.xiaolanshu.notice.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticeItemRspVO;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticePageReqVO;
import com.wsz.xiaolanshu.notice.biz.service.NoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 16:00
 * @Company:
 */
@RestController
@RequestMapping("/notice")
@Slf4j
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 分页获取通知列表
     * @param reqVO
     * @return
     */
    @PostMapping("/list")
    @ApiOperationLog(description = "分页获取通知列表")
    public Response<PageResponse<NoticeItemRspVO>> getNoticeList(@Validated @RequestBody NoticePageReqVO reqVO) {
        return Response.success(noticeService.getNoticeList(reqVO));
    }
}
