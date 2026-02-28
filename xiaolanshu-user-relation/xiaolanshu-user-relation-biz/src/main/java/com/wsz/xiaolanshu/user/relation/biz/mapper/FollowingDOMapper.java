package com.wsz.xiaolanshu.user.relation.biz.mapper;

import com.wsz.xiaolanshu.user.relation.biz.domain.dataobject.FollowingDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowingDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingDO record);

    int insertSelective(FollowingDO record);

    FollowingDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingDO record);

    int updateByPrimaryKey(FollowingDO record);

    List<FollowingDO> selectByUserId(Long userId);

    int deleteByUserIdAndFollowingUserId(@Param("userId") Long userId,
                                         @Param("unfollowUserId") Long unfollowUserId);

    /**
     * 查询记录总数
     *
     * @param userId
     * @return
     */
    long selectCountByUserId(Long userId);

    /**
     * 分页查询
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FollowingDO> selectPageListByUserId(@Param("userId") Long userId,
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);

    /**
     * 查询关注用户列表
     * @param userId
     * @return
     */
    List<FollowingDO> selectAllByUserId(Long userId);

    long checkFollowStatus(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    /**
     * 获取提到用户列表的 ID 集合
     * @param userId 当前用户ID
     * @param query 搜索关键词（可为空）
     * @param limit 限制数量（30）
     * @return 关注的用户ID列表
     */
    List<Long> selectMentionIds(@Param("userId") Long userId,
                                @Param("query") String query,
                                @Param("limit") Integer limit);
}