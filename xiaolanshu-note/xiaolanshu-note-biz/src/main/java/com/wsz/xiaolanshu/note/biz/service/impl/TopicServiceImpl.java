package com.wsz.xiaolanshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.TopicDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindTopicRspVO;
import com.wsz.xiaolanshu.note.biz.mapper.TopicDOMapper;
import com.wsz.xiaolanshu.note.biz.service.TopicService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-10 22:40
 * @Company:
 */
@Service
@Slf4j
public class TopicServiceImpl implements TopicService {

    @Resource
    private TopicDOMapper topicDOMapper;

    @Override
    public Response<List<FindTopicRspVO>> findTopicList(FindTopicListReqVO findTopicListReqVO) {
        String keyword = findTopicListReqVO.getKeyword();

        List<TopicDO> topicDOS = topicDOMapper.selectByLikeName(keyword);

        List<FindTopicRspVO> findTopicRspVOS = null;
        if (CollUtil.isNotEmpty(topicDOS)) {
            findTopicRspVOS = topicDOS.stream()
                    .map(topicDO -> FindTopicRspVO.builder()
                            .id(topicDO.getId())
                            .name(topicDO.getName())
                            .build())
                    .toList();
        }

        return Response.success(findTopicRspVOS);
    }
}
