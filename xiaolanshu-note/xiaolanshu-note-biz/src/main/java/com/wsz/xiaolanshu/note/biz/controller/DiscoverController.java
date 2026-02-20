package com.wsz.xiaolanshu.note.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNoteRspVO;
import com.wsz.xiaolanshu.note.biz.service.DiscoverService;
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
 * @Date 2026-02-13 18:44
 * @Company:
 */
@RestController
@RequestMapping("/discover")
@Slf4j
public class DiscoverController {

    @Resource
    private DiscoverService discoverService;

    @PostMapping(value = "/note/list")
    @ApiOperationLog(description = "发现页-查询笔记列表")
    public PageResponse<FindDiscoverNoteRspVO> findNoteList(@Validated @RequestBody FindDiscoverNotePageListReqVO findDiscoverNoteListReqVO) {
        return discoverService.findNoteList(findDiscoverNoteListReqVO);
    }

}