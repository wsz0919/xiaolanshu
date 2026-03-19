package com.wsz.xiaolanshu.note.biz.service.impl;

import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNoteRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminUpdateNoteStatusReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.service.AdminNoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public PageResponse<AdminNoteRspVO> findNotePageList(FindNotePageListReqVO reqVO) {
        long pageNo = reqVO.getPageNo();
        long pageSize = reqVO.getPageSize();
        // 1. 调用自定义分页工具类获取 offset
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 2. 查询符合条件的总数据量
        long totalCount = noteDOMapper.selectAdminTotalCount(reqVO.getTitle(), reqVO.getCreatorId(), reqVO.getStatus());

        List<AdminNoteRspVO> vos = new ArrayList<>();

        // 3. 如果总数大于0，再去查询当前页的数据，避免无效的查库
        if (totalCount > 0) {
            List<NoteDO> noteDOList = noteDOMapper.selectAdminPageList(
                    reqVO.getTitle(),
                    reqVO.getCreatorId(),
                    reqVO.getStatus(),
                    offset,
                    pageSize
            );

            // 4. DO 转换 为 VO
            vos = noteDOList.stream().map(noteDO -> {
                AdminNoteRspVO vo = new AdminNoteRspVO();
                vo.setId(noteDO.getId());
                vo.setTitle(noteDO.getTitle());
                vo.setCreatorId(noteDO.getCreatorId());
                vo.setType(noteDO.getType());
                vo.setStatus(noteDO.getStatus());
                vo.setVisible(noteDO.getVisible());
                vo.setCreateTime(noteDO.getCreateTime());
                return vo;
            }).collect(Collectors.toList());
        }

        // 5. 使用您自己封装的 success 方法返回分页结果
        return PageResponse.success(vos, pageNo, totalCount, pageSize);
    }

    @Override
    public Response<?> updateNoteStatus(AdminUpdateNoteStatusReqVO reqVO) {
        // 1. 根据业务要求封装 DO 进行选择性更新
        NoteDO noteDO = NoteDO.builder()
                .id(reqVO.getId())
                .status(reqVO.getStatus())
                .updateTime(LocalDateTime.now())
                .build();

        // 2. 更新数据库
        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);
        if (count == 0) {
            log.warn("笔记审核状态更新失败，笔记不存在. ID: {}", reqVO.getId());
            return Response.fail("笔记不存在或更新失败");
        }

        // 可扩展：如果审核通过/下架，可发送 MQ 消息去更新 ES 中的索引数据
        return Response.success();
    }


}
