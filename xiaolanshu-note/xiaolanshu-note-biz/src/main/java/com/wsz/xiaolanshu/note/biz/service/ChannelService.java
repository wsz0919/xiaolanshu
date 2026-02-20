package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindChannelRspVO;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 22:25
 * @Company:
 */
public interface ChannelService {

    /**
     * 查询所有频道
     * @return
     */
    Response<List<FindChannelRspVO>> findChannelList();

}
