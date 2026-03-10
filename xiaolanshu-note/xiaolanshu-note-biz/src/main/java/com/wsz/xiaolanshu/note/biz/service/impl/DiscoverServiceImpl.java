package com.wsz.xiaolanshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindDiscoverNoteRspVO;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.rpc.SearchRpcService;
import com.wsz.xiaolanshu.note.biz.service.DiscoverService;
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
 * @Date 2026-02-13 18:46
 * @Company:
 */
@Service
@Slf4j
public class DiscoverServiceImpl implements DiscoverService {

    @Resource
    private NoteDOMapper noteDOMapper;

    @Resource
    private SearchRpcService searchRpcService;


    @Override
    public PageResponse<FindDiscoverNoteRspVO> findNoteList(FindDiscoverNotePageListReqVO findDiscoverNoteListReqVO) {
        Long channelId = findDiscoverNoteListReqVO.getChannelId();
        Integer pageNo = findDiscoverNoteListReqVO.getPageNo();

        // 每页展示的数据量
        long pageSize = 10;

        // 1. 查询该频道下的笔记总数
        int count = noteDOMapper.selectTotalCount(channelId);

        // 若总数为 0，则直接响应
        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        // 计算分页查询的偏移量 offset
        long offset = PageResponse.getOffset(pageNo, pageSize);
        long totalPage = PageResponse.getTotalPage(count, pageSize);

        // 若请求的页码大于总页数，直接响应
        if (pageNo > totalPage) {
            return PageResponse.success(null, pageNo, totalPage);
        }

        // 2. 从数据库获取当前页的 NoteDO 集合，并提取 ID
        List<NoteDO> noteDOS = noteDOMapper.selectPageList(channelId, offset, pageSize);
        List<Long> noteIds = noteDOS.stream().map(NoteDO::getId).toList();

        // 3. 走 Elasticsearch 批量获取格式化好的笔记文档数据
        List<SearchNoteDTO> searchNoteDTOS = searchRpcService.searchNotesByIds(noteIds);

        List<FindDiscoverNoteRspVO> noteRspVOS = Lists.newArrayList();

        // 4. 将 ES 返回的数据转换为 VO，并保持数据库查询出的分页排序
        if (CollUtil.isNotEmpty(searchNoteDTOS)) {
            // 转为 Map，方便后续根据 noteId 快速提取数据
            Map<Long, SearchNoteDTO> noteIdToDTOMap = searchNoteDTOS.stream()
                    .collect(Collectors.toMap(SearchNoteDTO::getNoteId, dto -> dto));

            for (Long noteId : noteIds) {
                SearchNoteDTO searchNoteDTO = noteIdToDTOMap.get(noteId);

                if (Objects.nonNull(searchNoteDTO)) {
                    FindDiscoverNoteRspVO findDiscoverNoteRspVO = FindDiscoverNoteRspVO.builder()
                            .id(String.valueOf(searchNoteDTO.getNoteId()))
                            .title(searchNoteDTO.getTitle())
                            .type(searchNoteDTO.getType())
                            .cover(searchNoteDTO.getCover())
                            .videoUri(searchNoteDTO.getVideoUri())
                            .creatorId(searchNoteDTO.getCreatorId())
                            .nickname(searchNoteDTO.getNickname())
                            .avatar(searchNoteDTO.getAvatar())
                            // 点赞数转换
                            .likeTotal(searchNoteDTO.getLikeTotal() != null ? String.valueOf(searchNoteDTO.getLikeTotal()) : "0")
                            .build();

                    noteRspVOS.add(findDiscoverNoteRspVO);
                }
            }
        }

        return PageResponse.success(noteRspVOS, pageNo, count, pageSize);
    }
}