package com.wsz.xiaolanshu.search.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-06 14:47
 * @Company:
 */
public interface SelectMapper {

    /**
     * 查询笔记文档所需的全字段数据
     * @param noteId
     * @return
     */
    List<Map<String, Object>> selectEsNoteIndexData(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 查询用户索引所需的全字段数据
     * @param userId
     * @return
     */
    List<Map<String, Object>> selectEsUserIndexData(@Param("userId") Long userId);

}
