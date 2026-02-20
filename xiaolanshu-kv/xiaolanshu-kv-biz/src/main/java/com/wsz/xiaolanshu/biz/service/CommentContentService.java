package com.wsz.xiaolanshu.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.BatchFindCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.DeleteCommentContentReqDTO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 15:31
 * @Company:
 */
public interface CommentContentService {

    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

    /**
     * 删除评论内容
     * @param deleteCommentContentReqDTO
     * @return
     */
    Response<?> deleteCommentContent(DeleteCommentContentReqDTO deleteCommentContentReqDTO);

}
