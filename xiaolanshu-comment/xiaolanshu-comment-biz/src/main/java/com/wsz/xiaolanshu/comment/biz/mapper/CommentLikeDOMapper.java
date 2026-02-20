package com.wsz.xiaolanshu.comment.biz.mapper;

import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentLikeDO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.LikeUnlikeCommentMqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentLikeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentLikeDO record);

    int insertSelective(CommentLikeDO record);

    CommentLikeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentLikeDO record);

    int updateByPrimaryKey(CommentLikeDO record);

    /**
     * 查询某个评论是否被点赞
     *
     * @param userId
     * @param commentId
     * @return
     */
    int selectCountByUserIdAndCommentId(@Param("userId") Long userId,
                                        @Param("commentId") Long commentId);

    /**
     * 查询对应用户点赞的所有评论
     * @param userId
     * @return
     */
    List<CommentLikeDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 批量删除点赞记录
     * @param unlikes
     * @return
     */
    int batchDelete(@Param("unlikes") List<LikeUnlikeCommentMqDTO> unlikes);

    /**
     * 批量添加点赞记录
     * @param likes
     * @return
     */
    int batchInsert(@Param("likes") List<LikeUnlikeCommentMqDTO> likes);

    /**
     * 批量查询当前用户点赞过的评论 ID
     *
     * @param userId 当前用户 ID
     * @param commentIds 当前列表的评论 ID 集合
     * @return 点赞过的评论 ID 集合
     */
    List<Long> selectLikedCommentIds(@Param("userId") Long userId,
                                     @Param("commentIds") List<Long> commentIds);
}