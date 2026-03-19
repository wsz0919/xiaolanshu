package com.wsz.xiaolanshu.biz.service.impl;

import com.wsz.framework.common.exception.BizException;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.biz.domain.dataobject.NoteContentDO;
import com.wsz.xiaolanshu.biz.enums.ResponseCodeEnum;
import com.wsz.xiaolanshu.biz.repository.NoteContentRepository;
import com.wsz.xiaolanshu.biz.service.NoteContentService;
import com.wsz.xiaolanshu.kv.dto.req.AddNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.BatchFindNoteContentReqDTO;
import org.apache.commons.lang3.StringUtils;
import com.wsz.xiaolanshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-09 19:02
 * @Company:
 */
@Service
@Slf4j
public class NoteContentServiceImpl implements NoteContentService {

    @Resource
    private NoteContentRepository noteContentRepository;


    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        // 笔记 ID
        String uuid = addNoteContentReqDTO.getUuid();
        // 笔记内容
        String content = addNoteContentReqDTO.getContent();

        // 构建数据库 DO 实体类
        NoteContentDO nodeContent = NoteContentDO.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();

        // 插入数据
        noteContentRepository.save(nodeContent);

        return Response.success();
    }

    /**
     * 查询笔记内容
     *
     * @param findNoteContentReqDTO
     * @return
     */
    @Override
    public Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        // 笔记 ID
        String uuid = findNoteContentReqDTO.getUuid();
        // 根据笔记 ID 查询笔记内容
        Optional<NoteContentDO> optional = noteContentRepository.findById(UUID.fromString(uuid));

        // 若笔记内容不存在
        if (!optional.isPresent()) {
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        NoteContentDO noteContentDO = optional.get();
        // 构建返参 DTO
        FindNoteContentRspDTO findNoteContentRspDTO = FindNoteContentRspDTO.builder()
                .uuid(noteContentDO.getId())
                .content(noteContentDO.getContent())
                .build();

        return Response.success(findNoteContentRspDTO);
    }

    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        // 笔记 ID
        String uuid = deleteNoteContentReqDTO.getUuid();
        // 删除笔记内容
        noteContentRepository.deleteById(UUID.fromString(uuid));

        return Response.success();
    }

    /**
     * 批量查询笔记内容
     *
     * @param reqDTO 批量查询请求参数
     * @return 笔记内容列表
     */
    @Override
    public Response<List<FindNoteContentRspDTO>> findNoteContentBatch(BatchFindNoteContentReqDTO reqDTO) {
        List<String> uuids = reqDTO.getUuids();

        // 1. 判空校验
        if (uuids == null || uuids.isEmpty()) {
            return Response.success(Collections.emptyList());
        }

        // 2. 将 String 类型的 UUID 转换为 java.util.UUID 类型，并过滤掉空值
        List<UUID> uuidList = uuids.stream()
                .filter(StringUtils::isNotBlank)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        if (uuidList.isEmpty()) {
            return Response.success(Collections.emptyList());
        }

        // 3. 调用 Repository 的 findAllById 方法进行底层批量查询 (Cassandra/JPA 自带方法)
        Iterable<NoteContentDO> noteContentDOIterable = noteContentRepository.findAllById(uuidList);

        // 4. 遍历查询结果并转换为返回的 DTO 列表
        List<FindNoteContentRspDTO> rspDTOList = new ArrayList<>();
        noteContentDOIterable.forEach(noteContentDO -> {
            FindNoteContentRspDTO dto = FindNoteContentRspDTO.builder()
                    .uuid(noteContentDO.getId()) // 这里的 getId() 返回的是 UUID 类型
                    .content(noteContentDO.getContent())
                    .build();
            rspDTOList.add(dto);
        });

        // 5. 成功返回
        return Response.success(rspDTOList);
    }
}
