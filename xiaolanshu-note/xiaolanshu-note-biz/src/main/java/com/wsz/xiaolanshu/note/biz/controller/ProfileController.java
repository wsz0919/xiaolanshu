package com.wsz.xiaolanshu.note.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNoteRspVO;
import com.wsz.xiaolanshu.note.biz.service.ProfileService;
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
 * @Date 2026-02-13 18:54
 * @Company:
 */
@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    @Resource
    private ProfileService profileService;

    @PostMapping(value = "/note/list")
    @ApiOperationLog(description = "个人主页-查询笔记列表")
    public PageResponse<FindProfileNoteRspVO> findNoteList(@Validated @RequestBody FindProfileNotePageListReqVO findProfileNotePageListReqVO) {
        return profileService.findNoteList(findProfileNotePageListReqVO);
    }

}
