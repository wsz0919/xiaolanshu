package com.wsz.xiaolanshu.note.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicRspVO;
import com.wsz.xiaolanshu.note.biz.service.TopicService;
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
 * @Date 2026-02-10 22:38
 * @Company:
 */
@RestController
@RequestMapping("/topic")
@Slf4j
public class TopicController {

    @Resource
    private TopicService topicService;

    @PostMapping(value = "/list")
    @ApiOperationLog(description = "模糊查询话题列表")
    public Response<List<FindTopicRspVO>> findTopicList(@Validated @RequestBody FindTopicListReqVO findTopicListReqVO) {
        return topicService.findTopicList(findTopicListReqVO);
    }

}
