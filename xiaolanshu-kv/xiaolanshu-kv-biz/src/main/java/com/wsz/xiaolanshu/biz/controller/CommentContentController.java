package com.wsz.xiaolanshu.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.biz.service.CommentContentService;
import com.wsz.xiaolanshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.BatchFindCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.DeleteCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindCommentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindCommentContentRspDTO;
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
 * @Date 2026-02-07 15:32
 * @Company:
 */
@RestController
@RequestMapping("/kv")
@Slf4j
public class CommentContentController {

    @Resource
    private CommentContentService commentContentService;

    @PostMapping(value = "/comment/content/batchAdd")
    @ApiOperationLog(description = "批量存储评论内容")
    public Response<?> batchAddCommentContent(@Validated @RequestBody BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {
        return commentContentService.batchAddCommentContent(batchAddCommentContentReqDTO);
    }

    @PostMapping(value = "/comment/content/batchFind")
    @ApiOperationLog(description = "批量查询评论内容")
    public Response<?> batchFindCommentContent(@Validated @RequestBody BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        return commentContentService.batchFindCommentContent(batchFindCommentContentReqDTO);
    }

    @PostMapping(value = "/comment/content/delete")
    @ApiOperationLog(description = "删除评论内容")
    public Response<?> deleteCommentContent(@Validated @RequestBody DeleteCommentContentReqDTO deleteCommentContentReqDTO) {
        return commentContentService.deleteCommentContent(deleteCommentContentReqDTO);
    }

    @PostMapping(value = "/comment/content/getContent")
    @ApiOperationLog(description = "查询评论内容")
    public Response<FindCommentContentRspDTO> getCommentByCommentId(@Validated @RequestBody FindCommentReqDTO req) {
        return commentContentService.findCommentContent(req);
    }
}
