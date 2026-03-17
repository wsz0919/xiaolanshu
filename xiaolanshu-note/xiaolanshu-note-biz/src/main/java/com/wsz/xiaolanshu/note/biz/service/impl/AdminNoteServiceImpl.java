package com.wsz.xiaolanshu.note.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNotePageRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminOfflineNoteReqVO;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.service.AdminNoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员笔记服务实现类
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 13:30
 * @Company:
 */
@Service
@Slf4j
public class AdminNoteServiceImpl implements AdminNoteService {

    @Resource
    private NoteDOMapper noteDOMapper;

    @Override
    public PageResponse<AdminNotePageRspVO.AdminNoteItemRspVO> getNotePageList(AdminNotePageReqVO reqVO) {
        // 分页参数校验
        if (reqVO.getPageNo() == null || reqVO.getPageNo() < 1) {
            reqVO.setPageNo(1);
        }
        if (reqVO.getPageSize() == null || reqVO.getPageSize() < 1 || reqVO.getPageSize() > 100) {
            reqVO.setPageSize(10);
        }

        try {
            // 构建分页查询
            Page<NoteDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
            QueryWrapper<NoteDO> queryWrapper = new QueryWrapper<>();

            // 状态筛选
            if (reqVO.getStatus() != null) {
                queryWrapper.eq("status", reqVO.getStatus());
            }

            // 发布者筛选
            if (reqVO.getCreatorId() != null) {
                queryWrapper.eq("creator_id", reqVO.getCreatorId());
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc("create_time");

            // 执行查询
            IPage<NoteDO> notePage = noteDOMapper.selectPage(page, queryWrapper);

            // 转换为响应对象
            List<AdminNotePageRspVO.AdminNoteItemRspVO> itemList = notePage.getRecords().stream()
                    .map(note -> {
                        AdminNotePageRspVO.AdminNoteItemRspVO item = new AdminNotePageRspVO.AdminNoteItemRspVO();
                        item.setId(note.getId());
                        item.setTitle(note.getTitle());
                        item.setCreatorId(note.getCreatorId());
                        item.setCreateTime(note.getCreateTime());
                        item.setStatus(note.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());

            return Response.success(PageResponse.success(itemList, reqVO.getPageNo(), notePage.getTotal(), reqVO.getPageSize()));
        } catch (Exception e) {
            log.error("分页查询笔记失败", e);
            return Response.error("分页查询笔记失败: " + e.getMessage());
        }
    }

    @Override
    public Response<?> offlineNote(AdminOfflineNoteReqVO reqVO) {
        // 查询笔记
        NoteDO noteDO = noteDOMapper.selectById(reqVO.getNoteId());
        if (noteDO == null) {
            return Response.error("笔记不存在");
        }

        // 更新状态为3（被下架）
        noteDO.setStatus(3);
        noteDOMapper.updateById(noteDO);

        log.info("管理员下架笔记: noteId={}, title={}", noteDO.getId(), noteDO.getTitle());

        return Response.success("笔记下架成功");
    }
}
