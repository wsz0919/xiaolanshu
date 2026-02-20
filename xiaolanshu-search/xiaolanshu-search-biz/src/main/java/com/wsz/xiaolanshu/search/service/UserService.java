package com.wsz.xiaolanshu.search.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.search.domain.vo.SearchUserReqVO;
import com.wsz.xiaolanshu.search.domain.vo.SearchUserRspVO;
import com.wsz.xiaolanshu.search.dto.RebuildUserDocumentReqDTO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-04 13:46
 * @Company:
 */
public interface UserService {

    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);

    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);

}
