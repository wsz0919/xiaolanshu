package com.wsz.xiaolanshu.search.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.search.domain.vo.SearchNoteReqVO;
import com.wsz.xiaolanshu.search.domain.vo.SearchNoteRspVO;
import com.wsz.xiaolanshu.search.dto.RebuildNoteDocumentReqDTO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-04 14:14
 * @Company:
 */
public interface NoteService {

    /**
     * 搜索笔记
     * @param searchNoteReqVO
     * @return
     */
    PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO);

    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);

}
