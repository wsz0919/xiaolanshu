package com.wsz.xiaolanshu.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.kv.dto.req.AddNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindNoteContentRspDTO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-09 19:01
 * @Company:
 */
public interface NoteContentService {

    /**
     * 添加笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 查询笔记内容
     *
     * @param findNoteContentReqDTO
     * @return
     */
    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    /**
     * 删除笔记内容
     *
     * @param deleteNoteContentReqDTO
     * @return
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
