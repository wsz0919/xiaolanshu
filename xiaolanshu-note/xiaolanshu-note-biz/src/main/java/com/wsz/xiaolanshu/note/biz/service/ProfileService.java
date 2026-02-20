package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNoteRspVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:56
 * @Company:
 */
public interface ProfileService {

    PageResponse<FindProfileNoteRspVO> findNoteList(FindProfileNotePageListReqVO findProfileNotePageListReqVO);
}
