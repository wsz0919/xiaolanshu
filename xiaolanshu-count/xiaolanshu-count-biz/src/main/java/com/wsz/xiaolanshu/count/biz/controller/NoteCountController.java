package com.wsz.xiaolanshu.count.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.biz.service.NoteCountService;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdRspDTO;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:44
 * @Company:
 */
@RestController
@RequestMapping("/count")
@Slf4j
public class NoteCountController {

    @Resource
    private NoteCountService noteCountService;

    @PostMapping(value = "/note/data")
    @ApiOperationLog(description = "获取笔记计数数据")
    public Response<FindNoteCountByIdRspDTO> findNoteCountData(@Validated @RequestBody FindNoteCountByIdReqDTO findNoteCountByIdReqDTO) {
        return noteCountService.findNoteCountData(findNoteCountByIdReqDTO);
    }

}
