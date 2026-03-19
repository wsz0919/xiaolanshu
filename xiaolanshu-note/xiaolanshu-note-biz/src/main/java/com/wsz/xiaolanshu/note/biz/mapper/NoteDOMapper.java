package com.wsz.xiaolanshu.note.biz.mapper;

import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteDO record);

    int insertSelective(NoteDO record);

    NoteDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteDO record);

    int updateByPrimaryKey(NoteDO record);

    int updateVisibleOnlyMe(NoteDO noteDO);

    int updateIsTop(NoteDO noteDO);

    int selectCountByNoteId(Long noteId);

    /**
     * 查询笔记的发布者用户 ID
     * @param noteId
     * @return
     */
    Long selectCreatorIdByNoteId(Long noteId);

    /**
     * 查询个人主页已发布笔记列表
     * @param creatorId
     * @param cursor
     * @return
     */
    List<NoteDO> selectPublishedNoteListByUserIdAndCursor(@Param("creatorId") Long creatorId,
                                                          @Param("cursor") Long cursor);

    int selectTotalCount(Long channelId);

    List<NoteDO> selectPageList(@Param("channelId") Long channelId,
                                @Param("offset") long offset,
                                @Param("pageSize") long pageSize);

    List<NoteDO> selectByNoteIds(@Param("noteIds") List<Long> noteIds);

    List<NoteDO> selectPageListByCreatorId(@Param("creatorId") Long creatorId,
                                           @Param("offset") long offset,
                                           @Param("pageSize") long pageSize);

    int selectTotalCountByCreatorId(Long creatorId);

    /**
     * 后台：根据条件查询笔记总数
     */
    long selectAdminTotalCount(@Param("title") String title,
                               @Param("creatorId") Long creatorId,
                               @Param("status") Integer status);

    /**
     * 后台：根据条件分页查询笔记列表
     */
    List<NoteDO> selectAdminPageList(@Param("title") String title,
                                     @Param("creatorId") Long creatorId,
                                     @Param("status") Integer status,
                                     @Param("offset") long offset,
                                     @Param("pageSize") long pageSize);
}