package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminOfflineNoteReqVO;

/**
 * 管理员笔记服务接口
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 13:30
 * @Company:
 */
public interface AdminNoteService {

    /**
     * 分页查询笔记列表
     * @param reqVO
     * @return
     */
    PageResponse<AdminNotePageRspVO.AdminNoteItemRspVO> getNotePageList(AdminNotePageReqVO reqVO);

    /**
     * 下架笔记
     * @param reqVO
     * @return
     */
    Response<?> offlineNote(AdminOfflineNoteReqVO reqVO);
}
