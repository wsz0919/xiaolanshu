package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNoteRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminUpdateNoteStatusReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindNotePageListReqVO;

/**
 * 管理员笔记服务接口
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 13:30
 * @Company:
 */
public interface AdminNoteService {

    /**
     * 笔记分页查询
     */
    PageResponse<AdminNoteRspVO> findNotePageList(FindNotePageListReqVO reqVO);

    /**
     * 笔记审核（更新笔记状态）
     */
    Response<?> updateNoteStatus(AdminUpdateNoteStatusReqVO reqVO);

}
