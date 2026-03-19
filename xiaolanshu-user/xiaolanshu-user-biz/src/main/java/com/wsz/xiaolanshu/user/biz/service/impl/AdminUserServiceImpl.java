package com.wsz.xiaolanshu.user.biz.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.user.biz.constant.UserBanConstants;
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
        Long userId = reqVO.getId();
        int newStatus = reqVO.getStatus(); // 前端传来的最新计算好的状态值

        // 1. 查询旧状态，用于对比哪些封禁发生了变化
        UserDO oldUser = userDOMapper.selectByPrimaryKey(userId);
        if (oldUser == null) {
            return Response.fail("用户不存在");
        }
        int oldStatus = oldUser.getStatus() == null ? 0 : oldUser.getStatus();

        // 2. 更新数据库中的 status 字段
        UserDO userDO = UserDO.builder()
                .id(userId)
                .status(newStatus)
                .updateTime(LocalDateTime.now())
                .build();
        userDOMapper.updateByPrimaryKeySelective(userDO);

        // ================== 3. 同步状态到 Sa-Token (Redis) ==================

        // 3.1 全局封禁 (踢出 + 无法登录)
        if (hasBit(newStatus, UserBanConstants.GLOBAL_BAN) && !hasBit(oldStatus, UserBanConstants.GLOBAL_BAN)) {
            StpUtil.kickout(userId);
            StpUtil.disable(userId, -1);
            log.info("已全局封禁并踢出用户 ID: {}", userId);
        } else if (!hasBit(newStatus, UserBanConstants.GLOBAL_BAN) && hasBit(oldStatus, UserBanConstants.GLOBAL_BAN)) {
            StpUtil.untieDisable(userId); // 解除全局封禁
        }

        // 3.2 笔记发布与编辑
        syncSaTokenDisableState(userId, newStatus, oldStatus, UserBanConstants.BAN_PUBLISH_NOTE, "publish_note");

        // 3.3 评论发布
        syncSaTokenDisableState(userId, newStatus, oldStatus, UserBanConstants.BAN_PUBLISH_COMMENT, "publish_comment");

        // 3.4 点赞与收藏
        syncSaTokenDisableState(userId, newStatus, oldStatus, UserBanConstants.BAN_LIKE_COLLECT, "like_collect");

        // 3.5 修改资料
        syncSaTokenDisableState(userId, newStatus, oldStatus, UserBanConstants.BAN_UPDATE_PROFILE, "update_profile");

        // 注意：BAN_PROFILE_ACCESS(32) 和 BAN_SEARCH(64) 是被动限制，由查询业务层的代码控制，不需要放入 Sa-Token

        return Response.success();
    }

    /**
     * 判断状态整数中是否包含某个特定的位
     */
    private boolean hasBit(int status, int bitConstant) {
        return (status & bitConstant) == bitConstant;
    }

    /**
     * 比对新旧状态，自动对某个服务进行 disable 或 untieDisable
     */
    private void syncSaTokenDisableState(Long userId, int newStatus, int oldStatus, int bitConstant, String serviceName) {
        boolean isNowBanned = hasBit(newStatus, bitConstant);
        boolean wasBanned = hasBit(oldStatus, bitConstant);

        if (isNowBanned && !wasBanned) {
            StpUtil.disable(userId, serviceName, -1); // -1 表示永久封禁，直到手动解封
            log.info("已封禁用户 ID: {} 的 {} 权限", userId, serviceName);
        } else if (!isNowBanned && wasBanned) {
            StpUtil.untieDisable(userId, serviceName);
            log.info("已解封用户 ID: {} 的 {} 权限", userId, serviceName);
        }
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
