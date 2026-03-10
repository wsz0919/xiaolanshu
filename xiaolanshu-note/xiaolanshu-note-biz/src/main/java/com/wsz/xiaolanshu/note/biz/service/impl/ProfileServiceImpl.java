package com.wsz.xiaolanshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindProfileNoteRspVO;
import com.wsz.xiaolanshu.note.biz.enums.ProfileNoteTypeEnum;
import com.wsz.xiaolanshu.note.biz.mapper.NoteCollectionDOMapper;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.mapper.NoteLikeDOMapper;
import com.wsz.xiaolanshu.note.biz.rpc.SearchRpcService;
import com.wsz.xiaolanshu.note.biz.service.ProfileService;
import com.wsz.xiaolanshu.search.dto.SearchNoteDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 18:56
 * @Company:
 */
@Service
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private NoteDOMapper noteDOMapper;

    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;

    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;

    // 注入搜索 RPC 服务，去除了原有的 UserRpcService 和 NoteCountDOMapper
    @Resource
    private SearchRpcService searchRpcService;

    @Override
    public PageResponse<FindProfileNoteRspVO> findNoteList(FindProfileNotePageListReqVO findProfileNotePageListReqVO) {
        Integer queryType = findProfileNotePageListReqVO.getType();
        Integer pageNo = findProfileNotePageListReqVO.getPageNo();
        Long userId = findProfileNotePageListReqVO.getUserId();

        // 每页展示的数据量
        long pageSize = 10;

        ProfileNoteTypeEnum profileNoteTypeEnum = ProfileNoteTypeEnum.valueOf(queryType);

        List<FindProfileNoteRspVO> noteRspVOS = Lists.newArrayList();
        List<Long> noteIds = null;
        int count = 0;

        // 统一计算分页查询的偏移量 offset
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 1. 获取不同 Tab (全部/收藏/点赞) 下的总数及分页 ID 列表
        switch (profileNoteTypeEnum) {
            case ALL -> { // 查询所有笔记
                count = noteDOMapper.selectTotalCountByCreatorId(userId);

                PageResponse<FindProfileNoteRspVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                List<NoteDO> noteDOS = noteDOMapper.selectPageListByCreatorId(userId, offset, pageSize);
                noteIds = noteDOS.stream().map(NoteDO::getId).collect(Collectors.toList());
            }
            case COLLECTED -> { // 查询用户收藏的笔记
                count = noteCollectionDOMapper.selectTotalCountByUserId(userId);

                PageResponse<FindProfileNoteRspVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                noteIds = noteCollectionDOMapper.selectPageListByUserId(userId, offset, pageSize);
            }
            case LIKED -> { // 查询用户点赞的笔记
                count = noteLikeDOMapper.selectTotalCountByUserId(userId);

                PageResponse<FindProfileNoteRspVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                noteIds = noteLikeDOMapper.selectPageListByUserId(userId, offset, pageSize);
            }
        }

        // 2. 利用拿到的 noteIds 走 RPC 调用 Elasticsearch 搜索聚合数据
        if (CollUtil.isNotEmpty(noteIds)) {
            List<SearchNoteDTO> searchNoteDTOS = searchRpcService.searchNotesByIds(noteIds);

            if (CollUtil.isNotEmpty(searchNoteDTOS)) {
                // 将 ES 返回的数据转换为 Map，方便根据 ID 快速提取并保持原本的分页排序顺序
                Map<Long, SearchNoteDTO> noteIdToDTOMap = searchNoteDTOS.stream()
                        .collect(Collectors.toMap(SearchNoteDTO::getNoteId, dto -> dto));

                // 3. 分页返参，严格按照数据库返回的 noteIds 顺序（如最新收藏、最新点赞的时间倒序）组装
                for (Long noteId : noteIds) {
                    SearchNoteDTO searchNoteDTO = noteIdToDTOMap.get(noteId);

                    if (Objects.nonNull(searchNoteDTO)) {
                        FindProfileNoteRspVO findProfileNoteRspVO = FindProfileNoteRspVO.builder()
                                .id(String.valueOf(searchNoteDTO.getNoteId()))
                                .title(searchNoteDTO.getTitle())
                                .type(searchNoteDTO.getType())
                                .cover(searchNoteDTO.getCover())
                                .videoUri(searchNoteDTO.getVideoUri())
                                .creatorId(searchNoteDTO.getCreatorId())
                                .nickname(searchNoteDTO.getNickname())
                                .avatar(searchNoteDTO.getAvatar())
                                .likeTotal(searchNoteDTO.getLikeTotal() != null ? String.valueOf(searchNoteDTO.getLikeTotal()) : "0")
                                .build();

                        noteRspVOS.add(findProfileNoteRspVO);
                    }
                }
            }
        }

        return PageResponse.success(noteRspVOS, pageNo, count, pageSize);
    }

    private static PageResponse<FindProfileNoteRspVO> checkCountAndPageNo(int count, Integer pageNo, long pageSize) {
        // 若总数为 0，则直接响应
        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        long totalPage = PageResponse.getTotalPage(count, pageSize);

        // 若请求的页码大于总页数，直接响应
        if (pageNo > totalPage) {
            return PageResponse.success(null, pageNo, totalPage);
        }
        return null;
    }
}