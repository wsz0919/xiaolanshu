package com.wsz.xiaolanshu.notice.biz.service.impl;

import com.wsz.framework.biz.context.holder.LoginUserContextHolder;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.util.DateUtils;
import com.wsz.xiaolanshu.comment.api.CommentFeignApi;
import com.wsz.xiaolanshu.comment.dto.FindCommentByIdRspDTO;
import com.wsz.xiaolanshu.comment.dto.LikeCommentReqDTO;
import com.wsz.xiaolanshu.kv.api.KeyValueFeignApi;
import com.wsz.xiaolanshu.kv.dto.req.FindCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindCommentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindCommentContentRspDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindNoteContentRspDTO;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticeItemRspVO;
import com.wsz.xiaolanshu.notice.biz.domain.vo.NoticePageReqVO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import com.wsz.xiaolanshu.notice.biz.service.NoticeService;
import com.wsz.xiaolanshu.user.api.UserFeignApi;
import com.wsz.xiaolanshu.user.dto.req.FindUsersByIdsReqDTO;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByIdRspDTO;
import com.wsz.xiaolanshu.user.relation.api.UserRelationFeignApi;
import com.wsz.xiaolanshu.user.relation.dto.FollowUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 14:40
 * @Company:
 */
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeDOMapper noticeDOMapper;

    @Resource
    private UserFeignApi userFeignApi;

    @Resource
    private NoteFeignApi noteFeignApi;

    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    @Resource
    private CommentFeignApi commentFeignApi;

    @Resource
    private UserRelationFeignApi relationFeignApi;

    @Override
    public PageResponse<NoticeItemRspVO> getNoticeList(NoticePageReqVO reqVO) {
        Long currentUserId = LoginUserContextHolder.getUserId();
        Integer type = convertTabToType(reqVO.getTabId());

        // 1. 手动计算分页偏移量
        int pageSize = reqVO.getPageSize();
        int pageNo = reqVO.getPageNo();
        int offset = (pageNo - 1) * pageSize;

        // 2. 查总数 (原生)
        long total = noticeDOMapper.selectCountByReceiverIdAndType(currentUserId, type);

        // 如果没有数据，直接利用您的 PageResponse 返回空结果
        if (total == 0) {
            PageResponse<NoticeItemRspVO> emptyPage = new PageResponse<>();
            emptyPage.setTotalCount(0L);
            emptyPage.setPageNo(pageNo);
            // 如果您的 PageResponse 类中由于泛型无法识别 setData，请根据您自己 common 里的字段名（如 setList 或 setRecords）调整
            emptyPage.setData(Collections.emptyList());
            return emptyPage;
        }

        // 3. 原生分页查询
        List<NoticeDO> doList = noticeDOMapper.selectPageList(currentUserId, type, offset, pageSize);

        if (CollectionUtils.isEmpty(doList)) {
            PageResponse<NoticeItemRspVO> emptyPage = new PageResponse<>();
            emptyPage.setTotalCount(total);
            emptyPage.setPageNo(pageNo);
            emptyPage.setData(Collections.emptyList());
            return emptyPage;
        }

        // 4. 提取 Sender ID 批量获取用户信息
        List<Long> senderIds = doList.stream()
                .map(NoticeDO::getSenderId).distinct().collect(Collectors.toList());
        FindUsersByIdsReqDTO userReq = new FindUsersByIdsReqDTO();
        userReq.setIds(senderIds);
        List<FindUserByIdRspDTO> users = userFeignApi.findByIds(userReq).getData();
        Map<Long, FindUserByIdRspDTO> userMap = users != null ?
                users.stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, u -> u)) : new HashMap<>();

        // 5. 核心：组装为前端富文本视图
        List<NoticeItemRspVO> rspList = doList.stream().map(notice -> {
            LikeCommentReqDTO likeCommentReqDTO = new LikeCommentReqDTO();
            FindNoteDetailReqDTO noteReq = new FindNoteDetailReqDTO();

            NoticeItemRspVO item = new NoticeItemRspVO();
            item.setId(String.valueOf(notice.getId()));
            item.setTime(DateUtils.formatRelativeTime(notice.getCreateTime()));
            item.setActionText(getActionText(notice.getSubType()));

            FindUserByIdRspDTO sender = userMap.get(notice.getSenderId());
            NoticeItemRspVO.NoticeUserVO userVO = new NoticeItemRspVO.NoticeUserVO();
            if (sender != null) {
                userVO.setNickname(sender.getNickName());
                userVO.setAvatar(sender.getAvatar());
                userVO.setIsAuthor(false);
            }
            item.setUser(userVO);

            // 分 Tab 特殊数据装配
            if (type == 1) {
                item.setType("like");
                Long noteId = notice.getTargetId();

                if (notice.getSubType() == 13) {
                    likeCommentReqDTO.setCommentId(notice.getTargetId());
                    FindCommentByIdRspDTO comment = commentFeignApi.getNoteIdByCommentId(likeCommentReqDTO).getData();
                    if (comment != null) {
                        noteId = comment.getNoteId();
                        }
                }

                if (noteId != null && noteId > 0) {
                    noteReq.setId(noteId);
                    FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(noteReq).getData();
                    String cover = String.valueOf(note.getImgUris());
                    if (note != null && StringUtils.isNotBlank(cover)) {
                        item.setCover(cover.split(",")[0]);
                    }
                }
            }
            else if (type == 2) {
                item.setType("follow");
                FollowUserReqDTO followUserReqDTO = new FollowUserReqDTO();
                followUserReqDTO.setReceiverId(currentUserId);
                followUserReqDTO.setSenderId(notice.getSenderId());
                Boolean isMutual = (Boolean) relationFeignApi.isFollowOrUnfollow(followUserReqDTO).getData();
                item.setIsMutual(isMutual != null ? isMutual : false);
                item.setIsMutual(false);
            }
            else if (type == 3) {
                item.setType("reply");
                likeCommentReqDTO.setCommentId(notice.getTargetId());
                FindCommentByIdRspDTO comment = commentFeignApi.getNoteIdByCommentId(likeCommentReqDTO).getData();
                if (comment != null) {
                    FindCommentReqDTO kvReq = new FindCommentReqDTO();
                    kvReq.setContentUuid(comment.getContentUuid());
                    kvReq.setNoteId(comment.getNoteId());
                    kvReq.setYearMonth(DateUtils.parse2MonthStr(notice.getCreateTime()));
                    FindCommentContentRspDTO contentRsp = keyValueFeignApi.getCommentByCommentId(kvReq).getData();
                     if (contentRsp != null) item.setContent(contentRsp.getContent());

                     noteReq.setId(comment.getNoteId());
                     FindNoteDetailRspDTO note = noteFeignApi.findNoteDetail(noteReq).getData();
                     if (note != null) {
                         if(StringUtils.isNotBlank(String.valueOf(note.getImgUris()))) {
                             item.setCover(String.valueOf(note.getImgUris()).split(",")[0]);
                         }
                         userVO.setIsAuthor(notice.getSenderId().equals(note.getCreatorId()));

                         if (notice.getSubType() == 31) {
                             item.setQuoteText(note.getTitle());
                         } else if (notice.getSubType() == 32 && comment.getReplyCommentId() != null) {
                             likeCommentReqDTO.setCommentId(comment.getReplyCommentId());
                             FindCommentByIdRspDTO parentComment = commentFeignApi.getNoteIdByCommentId(likeCommentReqDTO).getData();
                             if(parentComment != null) {
                                 FindCommentReqDTO parentKvReq = new FindCommentReqDTO();
                                 parentKvReq.setContentUuid(parentComment.getContentUuid());
                                 parentKvReq.setNoteId(comment.getNoteId());
                                 parentKvReq.setYearMonth(DateUtils.parse2MonthStr(notice.getCreateTime()));
                                 FindCommentContentRspDTO parentContent = keyValueFeignApi.getCommentByCommentId(parentKvReq).getData();
                                 if (parentContent != null) item.setQuoteText(parentContent.getContent());
                             }
                         }
                     }
                }
            }
            return item;
        }).collect(Collectors.toList());

        // 6. 返回组装完毕的分页对象
        PageResponse<NoticeItemRspVO> pageResponse = new PageResponse<>();
        pageResponse.setPageSize(reqVO.getPageSize());
        pageResponse.setTotalCount(total);
        pageResponse.setPageNo(pageNo);
        pageResponse.setData(rspList); // *注意：若您 common 包中是 setList() 等，请视情况更换方法名
        return pageResponse;
    }

    // ================== 私有辅助方法 ==================

    private Integer convertTabToType(String tabId) {
        if ("like_collect".equals(tabId)) return 1;
        if ("follow".equals(tabId)) return 2;
        return 3;
    }

    private String getActionText(Integer subType) {

        if (subType == null) {
            return "与你产生了互动";
        }

        return switch (subType) {
            case 11 -> "赞了你的笔记";
            case 12 -> "收藏了你的笔记";
            case 13 -> "赞了你的评论";
            case 21 -> "开始关注你了";
            case 31 -> "评论了你的笔记";
            case 32 -> "回复了你的评论";
            default -> "与你产生了互动";
        };
    }
}
