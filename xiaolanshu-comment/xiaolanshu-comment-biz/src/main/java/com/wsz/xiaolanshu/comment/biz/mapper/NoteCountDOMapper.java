package com.wsz.xiaolanshu.comment.biz.mapper;

import com.wsz.xiaolanshu.comment.biz.domain.dataobject.NoteCountDO;
import org.apache.ibatis.annotations.Param;

public interface NoteCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountDO record);

    int insertSelective(NoteCountDO record);

    NoteCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountDO record);

    int updateByPrimaryKey(NoteCountDO record);

    /**
     * 查询笔记评论总数
     * @param noteId
     * @return
     */
    Long selectCommentTotalByNoteId(Long noteId);

    /**
     * 更新评论总数
     * @param noteId
     * @param count
     * @return
     */
    int updateCommentTotalByNoteId(@Param("noteId") Long noteId,
                                   @Param("count") int count);

    /**
     * 强一致性：插入或更新笔记的评论总数
     *
     * @param noteId 笔记ID
     * @param count  变动的数量 (正数增，负数减)
     * @return 影响的行数
     */
    int insertOrUpdateCommentTotalByNoteId(@Param("noteId") Long noteId, @Param("count") Integer count);
}