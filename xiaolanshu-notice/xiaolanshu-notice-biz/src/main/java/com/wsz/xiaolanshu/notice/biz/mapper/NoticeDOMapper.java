package com.wsz.xiaolanshu.notice.biz.mapper;

import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;

public interface NoticeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoticeDO record);

    int insertSelective(NoticeDO record);

    NoticeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoticeDO record);

    int updateByPrimaryKey(NoticeDO record);
}