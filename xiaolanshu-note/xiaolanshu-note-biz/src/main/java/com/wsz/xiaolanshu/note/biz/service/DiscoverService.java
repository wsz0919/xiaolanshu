package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNoteRspVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:46
 * @Company:
 */
public interface DiscoverService {

    PageResponse<FindDiscoverNoteRspVO> findNoteList(FindDiscoverNotePageListReqVO findDiscoverNoteListReqVO);

}
