package com.wsz.xiaolanshu.note.biz.convert;

import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.dto.PublishNoteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 17:37
 * @Company:
 */
@Mapper
public interface NoteConvert {

    /**
     * 初始化 convert 实例
     */
    NoteConvert INSTANCE = Mappers.getMapper(NoteConvert.class);

    /**
     * 将 DO 转化为 DTO
     * @param bean
     * @return
     */
    PublishNoteDTO convertDO2DTO(NoteDO bean);

    /**
     * 将 DTO 转化为 DO
     * @param bean
     * @return
     */
    NoteDO convertDTO2DO(PublishNoteDTO bean);
}
