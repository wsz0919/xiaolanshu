package com.wsz.xiaolanshu.notice.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wsz.framework.biz.context.holder.LoginUserContextHolder;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.util.DateUtils;
import com.wsz.xiaolanshu.comment.api.CommentFeignApi;
import com.wsz.xiaolanshu.comment.dto.FindCommentByIdRspDTO;
import com.wsz.xiaolanshu.comment.dto.LikeCommentReqDTO;
import com.wsz.xiaolanshu.kv.api.KeyValueFeignApi;
import com.wsz.xiaolanshu.kv.dto.req.FindCommentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindCommentContentRspDTO;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import com.wsz.xiaolanshu.notice.biz.constant.RedisConstants;
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
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 本地缓存：缓存评论/笔记基础信息，减少 RPC
     */
    private static final Cache<Long, FindCommentByIdRspDTO> COMMENT_CACHE = Caffeine.newBuilder()
            .initialCapacity(1000).maximumSize(5000).expireAfterWrite(1, TimeUnit.HOURS).build();

    private static final Cache<Long, FindNoteDetailRspDTO> NOTE_CACHE = Caffeine.newBuilder()
            .initialCapacity(1000).maximumSize(5000).expireAfterWrite(1, TimeUnit.HOURS).build();

    @Override
    public PageResponse<NoticeItemRspVO> getNoticeList(NoticePageReqVO reqVO) {
        Long currentUserId = LoginUserContextHolder.getUserId();
        Integer type = convertTabToType(reqVO.getTabId());
        int pageSize = reqVO.getPageSize();
        int pageNo = reqVO.getPageNo();
        int offset = (pageNo - 1) * pageSize;

        String redisKey = RedisConstants.buildNoticeZSetKey(currentUserId, type);
        List<NoticeDO> doList = new ArrayList<>();
        long total;

        // 1. Redis ZSet 逻辑保持不动
        Boolean hasKey = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(hasKey)) {
            total = redisTemplate.opsForZSet().zCard(redisKey);
            if (total > 0) {
                Set<Object> zSetIds = redisTemplate.opsForZSet().reverseRange(redisKey, offset, offset + pageSize - 1);
                if (!CollectionUtils.isEmpty(zSetIds)) {
                    List<Long> noticeIds = zSetIds.stream().map(id -> Long.valueOf(String.valueOf(id))).collect(Collectors.toList());
                    doList = noticeDOMapper.selectByIds(noticeIds);
                    Map<Long, NoticeDO> map = doList.stream().collect(Collectors.toMap(NoticeDO::getId, n -> n));
                    doList = noticeIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
                }
            }
        } else {
            total = noticeDOMapper.selectCountByReceiverIdAndType(currentUserId, type);
            if (total > 0) {
                doList = noticeDOMapper.selectPageList(currentUserId, type, offset, pageSize);
                rebuildZSetCacheAsync(currentUserId, type, redisKey);
            }
        }

        if (CollectionUtils.isEmpty(doList)) {
            return PageResponse.success(Collections.emptyList(), total, pageNo, pageSize);
        }

        // ================== 性能优化核心：批量并发预取 ==================

        // 2. 收集 ID（第一阶段）
        Set<Long> senderIds = doList.stream().map(NoticeDO::getSenderId).collect(Collectors.toSet());
        Set<Long> commentIdsToFetch = doList.stream()
                .filter(n -> n.getSubType() == 13 || type == 3) // 赞了评论 or 回复 Tab
                .map(NoticeDO::getTargetId).collect(Collectors.toSet());

        // 并行获取用户信息
        CompletableFuture<Map<Long, FindUserByIdRspDTO>> userMapFuture = CompletableFuture.supplyAsync(() -> {
            FindUsersByIdsReqDTO userReq = new FindUsersByIdsReqDTO();
            userReq.setIds(new ArrayList<>(senderIds));
            List<FindUserByIdRspDTO> users = userFeignApi.findByIds(userReq).getData();
            return users != null ? users.stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, u -> u)) : new HashMap<>();
        }, threadPoolTaskExecutor);

        // 并行获取评论详情（带 Caffeine）
        CompletableFuture<Map<Long, FindCommentByIdRspDTO>> commentMapFuture = CompletableFuture.supplyAsync(() ->
                batchFetchComments(commentIdsToFetch), threadPoolTaskExecutor);

        CompletableFuture.allOf(userMapFuture, commentMapFuture).join();
        Map<Long, FindUserByIdRspDTO> userMap = userMapFuture.join();
        Map<Long, FindCommentByIdRspDTO> commentMap = commentMapFuture.join();

        // 3. 收集笔记 ID（第二阶段，部分依赖第一阶段拿到的 NoteId）
        Set<Long> noteIdsToFetch = new HashSet<>();
        for (NoticeDO n : doList) {
            if (type == 1 && n.getSubType() != 13) noteIdsToFetch.add(n.getTargetId());
            FindCommentByIdRspDTO c = commentMap.get(n.getTargetId());
            if (c != null) {
                noteIdsToFetch.add(c.getNoteId());
            }
        }

        // 并行获取笔记详情（带 Caffeine）和互关状态
        CompletableFuture<Map<Long, FindNoteDetailRspDTO>> noteMapFuture = CompletableFuture.supplyAsync(() ->
                batchFetchNotes(noteIdsToFetch), threadPoolTaskExecutor);

        CompletableFuture<Map<Long, Boolean>> followFuture = (type == 2) ? CompletableFuture.supplyAsync(() ->
                batchFetchFollowStatus(currentUserId, senderIds), threadPoolTaskExecutor) : CompletableFuture.completedFuture(new HashMap<>());

        CompletableFuture.allOf(noteMapFuture, followFuture).join();
        Map<Long, FindNoteDetailRspDTO> noteMap = noteMapFuture.join();
        Map<Long, Boolean> followMap = followFuture.join();

        // ================== 数据装配：完全保留原始逻辑分支 ==================

        List<NoticeItemRspVO> rspList = doList.stream().map(notice -> {
            NoticeItemRspVO item = new NoticeItemRspVO();
            item.setId(String.valueOf(notice.getId()));
            item.setTime(DateUtils.formatRelativeTime(notice.getCreateTime()));
            item.setActionText(getActionText(notice.getSubType()));
            item.setSubType(notice.getSubType());
            item.setCurrentId(notice.getReceiverId());

            // 用户信息装配
            FindUserByIdRspDTO sender = userMap.get(notice.getSenderId());
            NoticeItemRspVO.NoticeUserVO userVO = new NoticeItemRspVO.NoticeUserVO();
            if (sender != null) {
                userVO.setUserId(notice.getSenderId());
                userVO.setNickname(sender.getNickName());
                userVO.setAvatar(sender.getAvatar());
                userVO.setIsAuthor(false);
            }
            item.setUser(userVO);

            // 分 Tab 逻辑（完全镜像原始逻辑）
            if (type == 1) {
                item.setType("like");
                Long noteId = notice.getTargetId();
                if (notice.getSubType() == 13) {
                    item.setCommentId(notice.getTargetId()); // 【关键赋值】：赞了这条评论的ID
                    FindCommentByIdRspDTO comment = commentMap.get(notice.getTargetId());
                    if (comment != null) {
                        noteId = comment.getNoteId();
                        item.setQuoteText(fetchKVContent(comment, notice.getCreateTime())); // KV 查询建议保留
                    }
                }
                item.setNoteId(noteId); // 【关键赋值】：不管是赞笔记还是赞评论，都统一提供 noteId 给前端
                fillNoteCover(item, noteMap.get(noteId));

            } else if (type == 2) {
                item.setType("follow");
                item.setIsMutual(followMap.getOrDefault(notice.getSenderId(), false));

            } else if (type == 3) {
                item.setType("reply");
                item.setCommentId(notice.getTargetId()); // 【关键赋值】：31/32/33 这里存的就是触发通知的评论ID

                FindCommentByIdRspDTO comment = commentMap.get(notice.getTargetId());
                if (comment != null) {
                    item.setContent(fetchKVContent(comment, notice.getCreateTime()));
                    item.setTargetId(notice.getTargetId());
                    item.setNoteId(comment.getNoteId()); // 设置 noteId 用于跳转
                    FindNoteDetailRspDTO note = noteMap.get(comment.getNoteId());
                    if (note != null) {
                        fillNoteCover(item, note);
                        userVO.setIsAuthor(notice.getSenderId().equals(note.getCreatorId()));
                        // 处理二级回复的 quoteText
                        if (notice.getSubType() == 32 && comment.getReplyCommentId() != null) {
                            item.setQuoteText(fetchParentCommentContent(comment.getReplyCommentId(), notice.getCreateTime()));
                        }
                    }
                }
            }
            return item;
        }).collect(Collectors.toList());

        return PageResponse.success(rspList, total, pageNo, pageSize);
    }

    // ================== 辅助批量方法 (保护逻辑 & 提升速度) ==================

    private Map<Long, FindCommentByIdRspDTO> batchFetchComments(Set<Long> ids) {
        Map<Long, FindCommentByIdRspDTO> result = new HashMap<>();
        List<Long> missIds = new ArrayList<>();
        for (Long id : ids) {
            FindCommentByIdRspDTO cache = COMMENT_CACHE.getIfPresent(id);
            if (cache != null) result.put(id, cache); else missIds.add(id);
        }
        if (!missIds.isEmpty()) {
            // 这里为了保证速度，依然开启并发拉取
            missIds.parallelStream().forEach(id -> {
                LikeCommentReqDTO req = new LikeCommentReqDTO();
                req.setCommentId(id);
                FindCommentByIdRspDTO data = commentFeignApi.getNoteIdByCommentId(req).getData();
                if (data != null) {
                    result.put(id, data);
                    COMMENT_CACHE.put(id, data);
                }
            });
        }
        return result;
    }

    private Map<Long, FindNoteDetailRspDTO> batchFetchNotes(Set<Long> ids) {
        Map<Long, FindNoteDetailRspDTO> result = new HashMap<>();
        List<Long> missIds = new ArrayList<>();
        for (Long id : ids) {
            FindNoteDetailRspDTO cache = NOTE_CACHE.getIfPresent(id);
            if (cache != null) result.put(id, cache); else missIds.add(id);
        }
        if (!missIds.isEmpty()) {
            missIds.parallelStream().forEach(id -> {
                FindNoteDetailReqDTO req = new FindNoteDetailReqDTO();
                req.setId(id);
                FindNoteDetailRspDTO data = noteFeignApi.findNoteDetail(req).getData();
                if (data != null) {
                    result.put(id, data);
                    NOTE_CACHE.put(id, data);
                }
            });
        }
        return result;
    }

    private Map<Long, Boolean> batchFetchFollowStatus(Long currentUserId, Set<Long> senderIds) {
        Map<Long, Boolean> result = new HashMap<>();
        senderIds.parallelStream().forEach(sid -> {
            FollowUserReqDTO req = new FollowUserReqDTO();
            req.setReceiverId(currentUserId);
            req.setSenderId(sid);
            Boolean isMutual = (Boolean) relationFeignApi.isFollowOrUnfollow(req).getData();
            result.put(sid, isMutual != null && isMutual);
        });
        return result;
    }

    private String fetchKVContent(FindCommentByIdRspDTO comment, LocalDateTime time) {
        FindCommentReqDTO kvReq = new FindCommentReqDTO();
        kvReq.setContentUuid(comment.getContentUuid());
        kvReq.setNoteId(comment.getNoteId());
        kvReq.setYearMonth(DateUtils.parse2MonthStr(time));
        FindCommentContentRspDTO contentRsp = keyValueFeignApi.getCommentByCommentId(kvReq).getData();
        return contentRsp != null ? contentRsp.getContent() : null;
    }

    private String fetchParentCommentContent(Long parentCommentId, LocalDateTime time) {
        // 由于二级回复内容查询频率较低，且逻辑较深，这里采用带缓存的单次拉取
        LikeCommentReqDTO req = new LikeCommentReqDTO();
        req.setCommentId(parentCommentId);
        FindCommentByIdRspDTO pc = commentFeignApi.getNoteIdByCommentId(req).getData();
        return pc != null ? fetchKVContent(pc, time) : null;
    }

    private void fillNoteCover(NoticeItemRspVO item, FindNoteDetailRspDTO note) {

        if (note == null) {
            return;
        }

        String image = String.valueOf(note.getImgUris());
        if (!"null".equals(image) && StringUtils.isNotBlank(image)) {
            String cover = String.valueOf(note.getImgUris());
            item.setCover(cover.split(",")[0]);
        } if (StringUtils.isNotBlank(note.getVideoUri())) {
            item.setCover(note.getVideoUri());
        }
    }

    // 原始辅助方法保持不变
    private Integer convertTabToType(String tabId) {
        if ("like_collect".equals(tabId)) return 1;
        if ("follow".equals(tabId)) return 2;
        return 3;
    }

    private String getActionText(Integer subType) {
        if (subType == null) return "与你产生了互动";
        return switch (subType) {
            case 11 -> "赞了你的笔记";
            case 12 -> "收藏了你的笔记";
            case 13 -> "赞了你的评论";
            case 21 -> "开始关注你了";
            case 31 -> "评论了你的笔记";
            case 32 -> "回复了你的评论";
            case 33 -> "在评论中@了你";
            default -> "与你产生了互动";
        };
    }

    private void rebuildZSetCacheAsync(Long userId, Integer type, String redisKey) {
        threadPoolTaskExecutor.execute(() -> {
            List<NoticeDO> recentNotices = noticeDOMapper.selectPageList(userId, type, 0, 500);
            if (!CollectionUtils.isEmpty(recentNotices)) {
                Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
                for (NoticeDO notice : recentNotices) {
                    long score = notice.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                    tuples.add(new DefaultTypedTuple<>(String.valueOf(notice.getId()), (double) score));
                }
                redisTemplate.opsForZSet().add(redisKey, tuples);
                redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
            }
        });
    }
}