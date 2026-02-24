package com.wsz.xiaolanshu.notice.biz.service;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticeItemRspVO;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticePageReqVO;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 14:40
 * @Company:
 */
public interface NoticeService {

    /**
     * 根据前端的 Tab 类型分页获取通知列表
     */
    PageResponse<NoticeItemRspVO> getNoticeList(NoticePageReqVO reqVO);

}
