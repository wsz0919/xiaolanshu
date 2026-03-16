package com.wsz.xiaolanshu.user.biz.mapper;

import com.wsz.xiaolanshu.user.biz.domain.dataobject.RoleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RoleDO record);

    int insertSelective(RoleDO record);

    RoleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleDO record);

    int updateByPrimaryKey(RoleDO record);

    /**
     * 查询所有被启用的角色
     *
     * @return
     */
    List<RoleDO> selectEnabledList();

    /**
     * 根据用户ID查询角色标识集合
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}