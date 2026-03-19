package com.wsz.xiaolanshu.user.biz.mapper;

import com.wsz.xiaolanshu.user.biz.domain.dataobject.UserDO;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByIdRspDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);

    /**
     * 根据手机号查询记录
     * @param phone
     * @return
     */
    UserDO selectByPhone(String phone);

    /**
     * 批量查询用户信息
     *
     * @param ids
     * @return
     */
    List<UserDO> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据小蓝书号查询用户
     * @param xiaolanshuId
     * @return
     */
    UserDO selectByXiaolanshuId(@Param("xiaolanshuId") String xiaolanshuId);

    /**
     * 查询最新注册/活跃的用户
     */
    List<FindUserByIdRspDTO> selectTopUsers(@Param("limit") int limit);

    /**
     * 后台：根据条件查询用户总数
     */
    long selectAdminTotalCount(@Param("phone") String phone,
                               @Param("xiaolanshuId") String xiaolanshuId,
                               @Param("status") Integer status);

    /**
     * 后台：根据条件分页查询用户列表
     */
    List<UserDO> selectAdminPageList(@Param("phone") String phone,
                                     @Param("xiaolanshuId") String xiaolanshuId,
                                     @Param("status") Integer status,
                                     @Param("offset") long offset,
                                     @Param("pageSize") long pageSize);

}