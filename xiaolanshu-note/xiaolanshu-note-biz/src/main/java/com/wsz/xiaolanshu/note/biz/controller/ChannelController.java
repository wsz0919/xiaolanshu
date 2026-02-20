package com.wsz.xiaolanshu.note.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindChannelRspVO;
import com.wsz.xiaolanshu.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 22:25
 * @Company:
 */
@RestController
@RequestMapping("/channel")
@Slf4j
public class ChannelController {

    @Resource
    private ChannelService channelService;

    @PostMapping(value = "/list")
    @ApiOperationLog(description = "获取所有频道")
    public Response<List<FindChannelRspVO>> findChannelList() {
        return channelService.findChannelList();
    }

}
