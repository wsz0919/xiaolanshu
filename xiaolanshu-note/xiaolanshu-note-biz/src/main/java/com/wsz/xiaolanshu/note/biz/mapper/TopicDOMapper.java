package com.wsz.xiaolanshu.note.biz.mapper;

import com.wsz.xiaolanshu.note.biz.domain.dataobject.TopicDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TopicDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicDO record);

    int insertSelective(TopicDO record);

    TopicDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDO record);

    int updateByPrimaryKey(TopicDO record);

    String selectNameByPrimaryKey(Long id);

    List<TopicDO> selectByLikeName(String keyword);

    int batchInsert(@Param("newTopics") List<TopicDO> newTopics);

    List<TopicDO> selectByTopicIdIn(List<Long> topicIds);

    TopicDO selectByTopicName(String name);
}