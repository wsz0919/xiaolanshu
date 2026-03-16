package com.wsz.xiaolanshu.user.biz.mapper;

import com.wsz.xiaolanshu.user.biz.domain.dataobject.PermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionDO record);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);

    int updateByPrimaryKey(PermissionDO record);

    /**
     * 查询 APP 端所有被启用的权限
     *
     * @return
     */
    List<PermissionDO> selectAppEnabledList();

    /**
     * 根据用户ID查询权限标识集合
     */
    List<String> selectPermissionKeysByUserId(@Param("userId") Long userId);
}