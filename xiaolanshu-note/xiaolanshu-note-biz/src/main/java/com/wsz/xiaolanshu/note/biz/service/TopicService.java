package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicRspVO;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 22:40
 * @Company:
 */
public interface TopicService {

    Response<List<FindTopicRspVO>> findTopicList(FindTopicListReqVO findTopicListReqVO);

}
