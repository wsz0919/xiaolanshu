package com.wsz.xiaolanshu.user.biz.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.domain.dataobject.UserDO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUpdateUserStatusReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageReqVO;
import com.wsz.xiaolanshu.user.biz.domain.vo.AdminUserPageRspVO;
import com.wsz.xiaolanshu.user.biz.mapper.UserDOMapper;
import com.wsz.xiaolanshu.user.biz.service.AdminUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-17 15:30
 * @Company:
 */
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private UserDOMapper userDOMapper;

    @Override
    public Response<?> banUser(AdminUpdateUserStatusReqVO reqVO) {
        // 1. 构建更新实体
        UserDO userDO = UserDO.builder()
                .id(reqVO.getId())
                .status(reqVO.getStatus())
                .updateTime(LocalDateTime.now())
                .build();

        // 2. 更新数据库
        int count = userDOMapper.updateByPrimaryKeySelective(userDO);
        if (count == 0) {
            log.warn("用户状态更新失败，用户不存在. ID: {}", reqVO.getId());
            return Response.fail("用户不存在或更新失败");
        }


        if (reqVO.getStatus() == 1) {
            // 通过 Sa-Token 踢出当前被封禁的用户
            StpUtil.kickout(reqVO.getId());
            StpUtil.disable(reqVO.getId(), -1);
            log.info("已封禁并踢出用户 ID: {}", reqVO.getId());
        }

        return Response.success();
    }

    @Override
    public PageResponse<AdminUserPageRspVO> getUserPageList(AdminUserPageReqVO reqVO) {
        long pageNo = reqVO.getPageNo();
        long pageSize = reqVO.getPageSize();
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 1. 查询总数
        long totalCount = userDOMapper.selectAdminTotalCount(
                reqVO.getPhone(), reqVO.getXiaolanshuId(), reqVO.getStatus()
        );

        List<AdminUserPageRspVO> vos = new ArrayList<>();

        // 2. 查询数据
        if (totalCount > 0) {
            List<UserDO> userDOList = userDOMapper.selectAdminPageList(
                    reqVO.getPhone(), reqVO.getXiaolanshuId(), reqVO.getStatus(),
                    offset, pageSize
            );

            // 3. DO 转 VO
            vos = userDOList.stream().map(userDO -> {
                AdminUserPageRspVO vo = new AdminUserPageRspVO();
                vo.setId(userDO.getId());
                vo.setXiaolanshuId(userDO.getXiaolanshuId());
                vo.setNickname(userDO.getNickname());
                vo.setAvatar(userDO.getAvatar());
                vo.setPhone(userDO.getPhone());
                vo.setSex(userDO.getSex());
                vo.setStatus(userDO.getStatus());
                vo.setCreateTime(userDO.getCreateTime());
                return vo;
            }).collect(Collectors.toList());
        }

        // 4. 返回分页结果
        return PageResponse.success(vos, pageNo, totalCount, pageSize);
    }
}
