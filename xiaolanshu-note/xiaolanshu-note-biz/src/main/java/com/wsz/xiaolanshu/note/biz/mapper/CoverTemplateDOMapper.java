package com.wsz.xiaolanshu.note.biz.mapper;

import com.wsz.xiaolanshu.note.biz.domain.dataobject.CoverTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-28 22:04
 * @Company:
 */
@Mapper
public interface CoverTemplateDOMapper {

    CoverTemplateDO selectById(Long id);

    List<CoverTemplateDO> selectAllEnabled();
}
