package com.wsz.xiaolanshu.note.biz.mapper;

import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteCountDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountDO record);

    int insertSelective(NoteCountDO record);

    NoteCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountDO record);

    int updateByPrimaryKey(NoteCountDO record);

    List<NoteCountDO> selectByNoteIds(@Param("noteIds") List<Long> noteIds);
}