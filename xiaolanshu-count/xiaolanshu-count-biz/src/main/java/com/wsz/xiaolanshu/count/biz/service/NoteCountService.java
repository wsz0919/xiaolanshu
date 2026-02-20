package com.wsz.xiaolanshu.count.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdRspDTO;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdReqDTO;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:37
 * @Company:
 */
public interface NoteCountService {

    /**
     * 查询笔记计数数据
     * @param findNoteCountByIdReqDTO
     * @return
     */
    Response<FindNoteCountByIdRspDTO> findNoteCountData(FindNoteCountByIdReqDTO findNoteCountByIdReqDTO);


}
