package com.wsz.xiaolanshu.notice.biz.mapper;

import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(NoticeDO record);

    NoticeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoticeDO record);

    int updateByPrimaryKey(NoticeDO record);

    int insert(NoticeDO notice);

    /**
     * 查询通知总数 (用于原生分页)
     */
    long selectCountByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("type") Integer type);

    /**
     * 原生分页查询列表
     */
    List<NoticeDO> selectPageList(@Param("receiverId") Long receiverId,
                                  @Param("type") Integer type,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);
}