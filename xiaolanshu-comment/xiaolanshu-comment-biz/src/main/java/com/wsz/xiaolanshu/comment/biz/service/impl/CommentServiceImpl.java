package com.wsz.xiaolanshu.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wsz.framework.biz.context.holder.LoginUserContextHolder;
import com.wsz.framework.common.constant.DateConstants;
import com.wsz.framework.common.exception.BizException;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.framework.common.util.DateUtils;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.comment.biz.constant.MQConstants;
import com.wsz.xiaolanshu.comment.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentDO;
import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentLikeDO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.FindCommentByIdRspDTO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.LikeCommentReqDTO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.LikeUnlikeCommentMqDTO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.PublishCommentMqDTO;
import com.wsz.xiaolanshu.comment.biz.domain.vo.*;
import com.wsz.xiaolanshu.comment.biz.enums.*;
import com.wsz.xiaolanshu.comment.biz.mapper.CommentDOMapper;
import com.wsz.xiaolanshu.comment.biz.mapper.CommentLikeDOMapper;
import com.wsz.xiaolanshu.comment.biz.mapper.NoteCountDOMapper;
import com.wsz.xiaolanshu.comment.biz.retry.SendMqRetryHelper;
import com.wsz.xiaolanshu.comment.biz.rpc.DistributedIdGeneratorRpcService;
import com.wsz.xiaolanshu.comment.biz.rpc.KeyValueRpcService;
import com.wsz.xiaolanshu.comment.biz.rpc.NoteRpcService;
import com.wsz.xiaolanshu.comment.biz.rpc.UserRpcService;
import com.wsz.xiaolanshu.comment.biz.service.CommentService;
import com.wsz.xiaolanshu.kv.dto.req.FindCommentContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindCommentContentRspDTO;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 14:03
 * @Company:
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource
    private CommentDOMapper commentDOMapper;

    @Resource
    private NoteCountDOMapper noteCountDOMapper;

    @Resource
    private KeyValueRpcService keyValueRpcService;

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private CommentLikeDOMapper commentLikeDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private NoteRpcService noteRpcService;


    /**
     * 评论详情本地缓存
     */
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();

    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        String content = publishCommentReqVO.getContent();
        String imageUrl = publishCommentReqVO.getImageUrl();

        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        Long creatorId = LoginUserContextHolder.getUserId();
        String commentId = distributedIdGeneratorRpcService.generateCommentId();

        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .commentId(Long.valueOf(commentId))
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyUserId(publishCommentReqVO.getReplyUserId())
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();

        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success(commentId);
    }

    @Override
    public PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        Long noteId = findCommentPageListReqVO.getNoteId();
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        long pageSize = 7;

        String noteCommentTotalKey = RedisConstants.buildNoteCommentTotalKey(noteId);
        Number commentTotal = (Number) redisTemplate.opsForHash().get(noteCommentTotalKey, RedisConstants.FIELD_COMMENT_TOTAL);
        long count = Objects.isNull(commentTotal) ? 0L : commentTotal.longValue();

        if (Objects.isNull(commentTotal)) {
            Long dbCount = noteCountDOMapper.selectCommentTotalByNoteId(noteId);
            count = Objects.isNull(dbCount) ? 0L : dbCount;
            long finalCount = count;
            threadPoolTaskExecutor.execute(() -> syncNoteCommentTotal2Redis(noteCommentTotalKey, finalCount));
        }

        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        List<FindCommentItemRspVO> commentRspVOS = Lists.newArrayList();
        long offset = PageResponse.getOffset(pageNo, pageSize);

        String commentZSetKey = RedisConstants.buildCommentListKey(noteId);
        boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(commentZSetKey));

        if (!hasKey) {
            threadPoolTaskExecutor.execute(() -> syncHeatComments2Redis(commentZSetKey, noteId));
        }

        if (hasKey && offset < 500) {
            Set<Object> commentIds = redisTemplate.opsForZSet().reverseRangeByScore(commentZSetKey, -Double.MAX_VALUE, Double.MAX_VALUE, offset, pageSize);

            if (CollUtil.isNotEmpty(commentIds)) {
                List<Object> commentIdList = Lists.newArrayList(commentIds);
                List<Long> localCacheExpiredCommentIds = Lists.newArrayList();
                List<Long> localCacheKeys = commentIdList.stream().map(commentId -> Long.valueOf(commentId.toString())).toList();

                Map<Long, String> commentIdAndDetailJsonMap = LOCAL_CACHE.getAll(localCacheKeys, missingKeys -> {
                    Map<Long, String> missingData = Maps.newHashMap();
                    missingKeys.forEach(key -> {
                        localCacheExpiredCommentIds.add(key);
                        missingData.put(key, Strings.EMPTY);
                    });
                    return missingData;
                });

                if (CollUtil.size(localCacheExpiredCommentIds) != commentIdList.size()) {
                    for (String value : commentIdAndDetailJsonMap.values()) {
                        if (StringUtils.isBlank(value)) continue;
                        commentRspVOS.add(JsonUtils.parseObject(value, FindCommentItemRspVO.class));
                    }
                }

                // 本地缓存全部命中，提前返回
                if (CollUtil.size(localCacheExpiredCommentIds) == 0) {
                    if (CollUtil.isNotEmpty(commentRspVOS)) {
                        setCommentCountData(commentRspVOS, localCacheExpiredCommentIds);
                        // ====== 修复点1：补上动态点赞状态 ======
                        setCommentIsLikedData(commentRspVOS);
                    }
                    return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
                }

                List<String> commentIdKeys = localCacheExpiredCommentIds.stream().map(RedisConstants::buildCommentDetailKey).toList();
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentIdKeys);
                List<Long> expiredCommentIds = Lists.newArrayList();

                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    Long commentId = Long.valueOf(localCacheExpiredCommentIds.get(i).toString());
                    if (Objects.nonNull(commentJson)) {
                        commentRspVOS.add(JsonUtils.parseObject(commentJson, FindCommentItemRspVO.class));
                    } else {
                        expiredCommentIds.add(commentId);
                    }
                }

                if (CollUtil.isNotEmpty(commentRspVOS)) setCommentCountData(commentRspVOS, expiredCommentIds);
                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDOS, noteId, commentRspVOS);
                }
            }

            commentRspVOS = commentRspVOS.stream().sorted(Comparator.comparing(FindCommentItemRspVO::getHeat).reversed()).collect(Collectors.toList());
            syncCommentDetail2LocalCache(commentRspVOS);

            // ====== 修复点2：Redis缓存命中提前返回前，补上动态点赞状态 ======
            if (CollUtil.isNotEmpty(commentRspVOS)) {
                setCommentIsLikedData(commentRspVOS);
            }

            return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
        }

        // 数据库兜底查询返回
        List<CommentDO> oneLevelCommentDOS = commentDOMapper.selectPageList(noteId, offset, pageSize);
        getCommentDataAndSync2Redis(oneLevelCommentDOS, noteId, commentRspVOS);
        syncCommentDetail2LocalCache(commentRspVOS);

        if (CollUtil.isNotEmpty(commentRspVOS)) {
            setCommentIsLikedData(commentRspVOS);
        }

        return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
    }

    private void syncNoteCommentTotal2Redis(String noteCommentTotalKey, Long dbCount) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForHash().put(noteCommentTotalKey, RedisConstants.FIELD_COMMENT_TOTAL, dbCount);
                long expireTime = 60*60 + RandomUtil.randomInt(4*60*60);
                operations.expire(noteCommentTotalKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    @Override
    public PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        Long parentCommentId = findChildCommentPageListReqVO.getParentCommentId();
        Integer pageNo = findChildCommentPageListReqVO.getPageNo();
        long pageSize = 6;

        String countCommentKey = RedisConstants.buildCountCommentKey(parentCommentId);
        Number redisCount = (Number) redisTemplate.opsForHash().get(countCommentKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL);
        long count = Objects.isNull(redisCount) ? 0L : redisCount.longValue();

        if (Objects.isNull(redisCount)) {
            Long dbCount = commentDOMapper.selectChildCommentTotalById(parentCommentId);
            if (Objects.isNull(dbCount)) {
                throw new BizException(ResponseCodeEnum.PARENT_COMMENT_NOT_FOUND);
            }
            count = dbCount;
            threadPoolTaskExecutor.execute(() -> syncCommentCount2Redis(countCommentKey, dbCount));
        }

        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        List<FindChildCommentItemRspVO> childCommentRspVOS = Lists.newArrayList();
        long offset = PageResponse.getOffset(pageNo, pageSize) + 1;

        String childCommentZSetKey = RedisConstants.buildChildCommentListKey(parentCommentId);
        boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(childCommentZSetKey));

        if (!hasKey) {
            threadPoolTaskExecutor.execute(() -> syncChildComments2Redis(parentCommentId, childCommentZSetKey));
        }

        if (hasKey && offset < 6*10) {
            Set<Object> childCommentIds = redisTemplate.opsForZSet().rangeByScore(childCommentZSetKey, 0, Double.MAX_VALUE, offset, pageSize);

            if (CollUtil.isNotEmpty(childCommentIds)) {
                List<Object> childCommentIdList = Lists.newArrayList(childCommentIds);
                List<String> commentIdKeys = childCommentIds.stream().map(RedisConstants::buildCommentDetailKey).toList();
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentIdKeys);
                List<Long> expiredChildCommentIds = Lists.newArrayList();

                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    Long commentId = Long.valueOf(childCommentIdList.get(i).toString());
                    if (Objects.nonNull(commentJson)) {
                        childCommentRspVOS.add(JsonUtils.parseObject(commentJson, FindChildCommentItemRspVO.class));
                    } else {
                        expiredChildCommentIds.add(commentId);
                    }
                }

                if (CollUtil.isNotEmpty(childCommentRspVOS)) setChildCommentCountData(childCommentRspVOS, expiredChildCommentIds);

                if (CollUtil.isNotEmpty(expiredChildCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredChildCommentIds);
                    getChildCommentDataAndSync2Redis(commentDOS, childCommentRspVOS);
                }

                childCommentRspVOS = childCommentRspVOS.stream().sorted(Comparator.comparing(FindChildCommentItemRspVO::getCommentId)).collect(Collectors.toList());

                // ====== 修复点3：二级评论缓存命中提前返回前，补上动态点赞状态 ======
                if (CollUtil.isNotEmpty(childCommentRspVOS)) {
                    setChildCommentIsLikedData(childCommentRspVOS);
                }

                return PageResponse.success(childCommentRspVOS, pageNo, count, pageSize);
            }
        }

        // 数据库兜底查询返回
        List<CommentDO> childCommentDOS = commentDOMapper.selectChildPageList(parentCommentId, offset, pageSize);
        getChildCommentDataAndSync2Redis(childCommentDOS, childCommentRspVOS);

        if (CollUtil.isNotEmpty(childCommentRspVOS)) {
            setChildCommentIsLikedData(childCommentRspVOS);
        }

        return PageResponse.success(childCommentRspVOS, pageNo, count, pageSize);
    }

    @Override
    public Response<?> likeComment(LikeCommentReqVO likeCommentReqVO) {
        Long commentId = likeCommentReqVO.getCommentId();
        checkCommentIsExist(commentId);
        Long userId = LoginUserContextHolder.getUserId();

        Long creatorId = commentDOMapper.getUserIdByCommentId(likeCommentReqVO.getCommentId());

        if (Objects.equals(creatorId, userId)) {
            throw new BizException(ResponseCodeEnum.CANT_LIKE_OWN_COMMENT);
        }

        String bloomUserCommentLikeListKey = RedisConstants.buildBloomCommentLikesKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_comment_like_check.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(bloomUserCommentLikeListKey), commentId);
        CommentLikeLuaResultEnum commentLikeLuaResultEnum = CommentLikeLuaResultEnum.valueOf(result);

        if (Objects.isNull(commentLikeLuaResultEnum)) throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);

        switch (commentLikeLuaResultEnum) {
            case NOT_EXIST -> {
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                long expireSeconds = 60*60 + RandomUtil.randomInt(60*60);
                if (count > 0) {
                    threadPoolTaskExecutor.submit(() -> batchAddCommentLike2BloomAndExpire(userId, expireSeconds, bloomUserCommentLikeListKey));
                    throw new BizException(ResponseCodeEnum.COMMENT_ALREADY_LIKED);
                }
                batchAddCommentLike2BloomAndExpire(userId, expireSeconds, bloomUserCommentLikeListKey);
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_comment_like_and_expire.lua")));
                script.setResultType(Long.class);
                redisTemplate.execute(script, Collections.singletonList(bloomUserCommentLikeListKey), commentId, expireSeconds);
            }
            case COMMENT_LIKED -> {
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                if (count > 0) throw new BizException(ResponseCodeEnum.COMMENT_ALREADY_LIKED);
            }
        }

        LikeUnlikeCommentMqDTO likeUnlikeCommentMqDTO = LikeUnlikeCommentMqDTO.builder()
                .userId(userId).commentId(commentId).type(LikeUnlikeCommentTypeEnum.LIKE.getCode()).commentCreatorId(creatorId).createTime(LocalDateTime.now()).build();

        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeCommentMqDTO)).build();
        String destination = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_LIKE;
        rocketMQTemplate.asyncSendOrderly(destination, message, String.valueOf(userId), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {}
            @Override
            public void onException(Throwable throwable) {}
        });

        return Response.success();
    }

    @Override
    public Response<?> unlikeComment(UnLikeCommentReqVO unLikeCommentReqVO) {
        Long commentId = unLikeCommentReqVO.getCommentId();
        checkCommentIsExist(commentId);
        Long userId = LoginUserContextHolder.getUserId();
        String bloomUserCommentLikeListKey = RedisConstants.buildBloomCommentLikesKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_comment_unlike_check.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(bloomUserCommentLikeListKey), commentId);
        CommentUnlikeLuaResultEnum commentUnlikeLuaResultEnum = CommentUnlikeLuaResultEnum.valueOf(result);

        if (Objects.isNull(commentUnlikeLuaResultEnum)) throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);

        switch (commentUnlikeLuaResultEnum) {
            case NOT_EXIST -> {
                threadPoolTaskExecutor.submit(() -> {
                    long expireSeconds = 60*60 + RandomUtil.randomInt(60*60);
                    batchAddCommentLike2BloomAndExpire(userId, expireSeconds, bloomUserCommentLikeListKey);
                });
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                if (count == 0) throw new BizException(ResponseCodeEnum.COMMENT_NOT_LIKED);
            }
            case COMMENT_NOT_LIKED -> throw new BizException(ResponseCodeEnum.COMMENT_NOT_LIKED);
        }

        LikeUnlikeCommentMqDTO likeUnlikeCommentMqDTO = LikeUnlikeCommentMqDTO.builder()
                .userId(userId).commentId(commentId).type(LikeUnlikeCommentTypeEnum.UNLIKE.getCode()).createTime(LocalDateTime.now()).build();

        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeCommentMqDTO)).build();
        String destination = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_UNLIKE;
        rocketMQTemplate.asyncSendOrderly(destination, message, String.valueOf(userId), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {}
            @Override
            public void onException(Throwable throwable) {}
        });

        return Response.success();
    }

    private void recurrentGetReplyCommentId(List<Long> commentIds, Long commentId) {
        List<CommentDO> replyCommentDOs = commentDOMapper.selectListByReplyCommentId(commentId);
        if (CollUtil.isEmpty(replyCommentDOs)) return;
        for (CommentDO replyCommentDO : replyCommentDOs) {
            commentIds.add(replyCommentDO.getId());
            recurrentGetReplyCommentId(commentIds, replyCommentDO.getId());
        }
    }

    private void sendDeleteLocalCacheMq(Long id) {
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, id, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {}
            @Override
            public void onException(Throwable throwable) {}
        });
    }

    @Override
    public Response<?> deleteComment(DeleteCommentReqVO deleteCommentReqVO) {
        Long commentId = deleteCommentReqVO.getCommentId();

        // 1. 校验评论是否存在
        CommentDO commentDO = commentDOMapper.selectByPrimaryKey(commentId);
        if (Objects.isNull(commentDO)) {
            throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
        }

        // 2. 校验权限
        Long currUserId = LoginUserContextHolder.getUserId();
        Long commentUserId = commentDO.getUserId();
        Long noteCreatorId = noteRpcService.getNoteCreatorId(commentDO.getNoteId());

        if (!Objects.equals(currUserId, commentUserId) && !Objects.equals(currUserId, noteCreatorId)) {
            throw new BizException(ResponseCodeEnum.COMMENT_CANT_OPERATE);
        }

        Integer level = commentDO.getLevel();
        Long noteId = commentDO.getNoteId();
        Long parentCommentId = commentDO.getParentId();

        // 3. 提取需要删除的所有评论 ID (包含自身和所有级联子评论)
        List<Long> allNeedDeleteCommentIds = Lists.newArrayList();
        allNeedDeleteCommentIds.add(commentId);

        int deleteCount = 0;

        if (Objects.equals(level, CommentLevelEnum.TWO.getCode())) {
            recurrentGetReplyCommentId(allNeedDeleteCommentIds, commentId);
            deleteCount = allNeedDeleteCommentIds.size();
        } else {
            Long childTotal = commentDO.getChildCommentTotal();
            deleteCount = 1 + (childTotal != null ? childTotal.intValue() : 0);
        }

        final int finalDeleteCount = deleteCount;

        // 4. 【强一致性：同步执行数据库删除与元数据扣减】
        transactionTemplate.execute(status -> {
            try {
                if (Objects.equals(level, CommentLevelEnum.ONE.getCode())) {
                    commentDOMapper.deleteByPrimaryKey(commentId);
                    commentDOMapper.deleteByParentId(commentId);
                } else {
                    commentDOMapper.deleteByIds(allNeedDeleteCommentIds);
                    commentDOMapper.updateChildCommentTotalById(parentCommentId, -finalDeleteCount);

                    CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(parentCommentId);
                    Long earliestCommentId = Objects.nonNull(earliestCommentDO) ? earliestCommentDO.getId() : 0L;
                    commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliestCommentId, parentCommentId);
                }

                noteCountDOMapper.insertOrUpdateCommentTotalByNoteId(noteId, -finalDeleteCount);
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("删除评论的数据库事务操作异常", ex);
                throw ex;
            }
        });

        try {
            keyValueRpcService.deleteCommentContent(commentDO.getNoteId(), commentDO.getCreateTime(), commentDO.getContentUuid());
        } catch (Exception e) {
            log.error("删除KV存储评论内容异常", e);
        }

        String noteCommentTotalKey = RedisConstants.buildNoteCommentTotalKey(noteId);
        boolean hasNoteCommentTotalKey = Boolean.TRUE.equals(redisTemplate.hasKey(noteCommentTotalKey));

        String countCommentKey = RedisConstants.buildCountCommentKey(parentCommentId);
        boolean hasCountCommentKey = Objects.equals(level, CommentLevelEnum.TWO.getCode()) && Boolean.TRUE.equals(redisTemplate.hasKey(countCommentKey));

        // 5. 同步强力清理所有相关的 Redis 缓存
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                if (hasNoteCommentTotalKey) {
                    operations.opsForHash().increment(noteCommentTotalKey, RedisConstants.FIELD_COMMENT_TOTAL, -finalDeleteCount);
                }

                if (Objects.equals(level, CommentLevelEnum.ONE.getCode())) {
                    String zsetKey = RedisConstants.buildCommentListKey(noteId);
                    operations.opsForZSet().remove(zsetKey, commentId);
                    operations.delete(RedisConstants.buildCommentDetailKey(commentId));
                    operations.delete(RedisConstants.buildChildCommentListKey(commentId));
                    operations.delete(RedisConstants.buildCountCommentKey(commentId));
                    operations.delete(RedisConstants.buildHaveFirstReplyCommentKey(commentId));
                } else {
                    String zsetKey = RedisConstants.buildChildCommentListKey(parentCommentId);
                    for (Long idToRemove : allNeedDeleteCommentIds) {
                        operations.opsForZSet().remove(zsetKey, idToRemove);
                        operations.delete(RedisConstants.buildCommentDetailKey(idToRemove));
                    }

                    if (hasCountCommentKey) {
                        operations.opsForHash().increment(countCommentKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL, -finalDeleteCount);
                    }

                    operations.delete(RedisConstants.buildCommentDetailKey(parentCommentId));
                }
                return null;
            }
        });

        // 6. 二级评论额外处理：处理首评标记和重新计算热度值
        if (Objects.equals(level, CommentLevelEnum.TWO.getCode())) {
            CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(parentCommentId);
            if (Objects.isNull(earliestCommentDO)) {
                redisTemplate.delete(RedisConstants.buildHaveFirstReplyCommentKey(parentCommentId));
            }

            Set<Long> commentIds = Sets.newHashSetWithExpectedSize(1);
            commentIds.add(parentCommentId);
            Message<String> heatMsg = MessageBuilder.withPayload(JsonUtils.toJsonString(commentIds)).build();
            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COMMENT_HEAT_UPDATE, heatMsg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {}
                @Override
                public void onException(Throwable throwable) {}
            });
        }

        // 7. 广播通知各个节点删除本地缓存
        if (Objects.equals(level, CommentLevelEnum.ONE.getCode())) {
            sendDeleteLocalCacheMq(commentId);
        } else {
            for (Long idToRemove : allNeedDeleteCommentIds) {
                sendDeleteLocalCacheMq(idToRemove);
            }
            sendDeleteLocalCacheMq(parentCommentId);
        }

        return Response.success();
    }

    @Override
    public void deleteCommentLocalCache(Long commentId) {
        LOCAL_CACHE.invalidate(commentId);
    }

    private void batchAddCommentLike2BloomAndExpire(Long userId, long expireSeconds, String bloomUserCommentLikeListKey) {
        try {
            List<CommentLikeDO> commentLikeDOS = commentLikeDOMapper.selectByUserId(userId);
            if (CollUtil.isNotEmpty(commentLikeDOS)) {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_comment_like_and_expire.lua")));
                script.setResultType(Long.class);
                List<Object> luaArgs = Lists.newArrayList();
                commentLikeDOS.forEach(commentLikeDO -> luaArgs.add(commentLikeDO.getCommentId()));
                luaArgs.add(expireSeconds);
                redisTemplate.execute(script, Collections.singletonList(bloomUserCommentLikeListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【评论点赞】布隆过滤器异常: ", e);
        }
    }

    private void checkCommentIsExist(Long commentId) {
        String localCacheJson = LOCAL_CACHE.getIfPresent(commentId);
        if (StringUtils.isBlank(localCacheJson)) {
            String commentDetailRedisKey = RedisConstants.buildCommentDetailKey(commentId);
            boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(commentDetailRedisKey));
            if (!hasKey) {
                CommentDO commentDO = commentDOMapper.selectByPrimaryKey(commentId);
                if (Objects.isNull(commentDO)) throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
            }
        }
    }

    private void setChildCommentCountData(List<FindChildCommentItemRspVO> commentRspVOS, List<Long> expiredCommentIds) {
        List<Long> notExpiredCommentIds = Lists.newArrayList();
        commentRspVOS.forEach(commentRspVO -> notExpiredCommentIds.add(commentRspVO.getCommentId()));
        Map<Long, Map<Object, Object>> commentIdAndCountMap = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);
        for (FindChildCommentItemRspVO commentRspVO : commentRspVOS) {
            Long commentId = commentRspVO.getCommentId();
            if (CollUtil.isNotEmpty(expiredCommentIds) && expiredCommentIds.contains(commentId)) continue;
            Map<Object, Object> hash = commentIdAndCountMap.get(commentId);
            if (CollUtil.isNotEmpty(hash)) {
                Long likeTotal = Long.valueOf(hash.get(RedisConstants.FIELD_LIKE_TOTAL).toString());
                commentRspVO.setLikeTotal(likeTotal);
            }
        }
    }

    private Map<Long, Map<Object, Object>> getCommentCountDataAndSync2RedisHash(List<Long> notExpiredCommentIds) {
        List<Long> expiredCountCommentIds = Lists.newArrayList();
        List<String> commentCountKeys = notExpiredCommentIds.stream().map(RedisConstants::buildCountCommentKey).toList();
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                commentCountKeys.forEach(key -> operations.opsForHash().entries(key));
                return null;
            }
        });
        Map<Long, Map<Object, Object>> commentIdAndCountMap = Maps.newHashMap();
        for (int i = 0; i < notExpiredCommentIds.size(); i++) {
            Long currCommentId = Long.valueOf(notExpiredCommentIds.get(i).toString());
            Map<Object, Object> hash = (Map<Object, Object>) results.get(i);
            if (CollUtil.isEmpty(hash)) {
                expiredCountCommentIds.add(currCommentId);
                continue;
            }
            commentIdAndCountMap.put(currCommentId, hash);
        }
        if (CollUtil.size(expiredCountCommentIds) > 0) {
            List<CommentDO> commentDOS = commentDOMapper.selectCommentCountByIds(expiredCountCommentIds);
            commentDOS.forEach(commentDO -> {
                Integer level = commentDO.getLevel();
                Map<Object, Object> map = Maps.newHashMap();
                map.put(RedisConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal());
                if (Objects.equals(level, CommentLevelEnum.ONE.getCode())) {
                    map.put(RedisConstants.FIELD_CHILD_COMMENT_TOTAL, commentDO.getChildCommentTotal());
                }
                commentIdAndCountMap.put(commentDO.getId(), map);
            });
            threadPoolTaskExecutor.execute(() -> {
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        commentDOS.forEach(commentDO -> {
                            String key = RedisConstants.buildCountCommentKey(commentDO.getId());
                            Integer level = commentDO.getLevel();
                            Map<String, Long> fieldsMap = Objects.equals(level, CommentLevelEnum.ONE.getCode()) ?
                                    Map.of(RedisConstants.FIELD_CHILD_COMMENT_TOTAL, commentDO.getChildCommentTotal(), RedisConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal()) : Map.of(RedisConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal());
                            operations.opsForHash().putAll(key, fieldsMap);
                            long expireTime = RandomUtil.randomInt(5 * 60 * 60);
                            operations.expire(key, expireTime, TimeUnit.SECONDS);
                        });
                        return null;
                    }
                });
            });
        }
        return commentIdAndCountMap;
    }

    private void getChildCommentDataAndSync2Redis(List<CommentDO> childCommentDOS, List<FindChildCommentItemRspVO> childCommentRspVOS) {
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        Set<Long> userIds = Sets.newHashSet();
        Long noteId = null;

        for (CommentDO childCommentDO : childCommentDOS) {
            noteId = childCommentDO.getNoteId();
            boolean isContentEmpty = childCommentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(childCommentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(childCommentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            userIds.add(childCommentDO.getUserId());
            Long parentId = childCommentDO.getParentId();
            Long replyCommentId = childCommentDO.getReplyCommentId();
            if (!Objects.equals(parentId, replyCommentId)) {
                userIds.add(childCommentDO.getReplyUserId());
            }
        }

        List<FindCommentContentRspDTO> findCommentContentRspDTOS = keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentUuidAndContentMap = findCommentContentRspDTOS.stream().collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
        }

        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds.stream().toList());
        Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            userIdAndDTOMap = findUserByIdRspDTOS.stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
        }

        for (CommentDO childCommentDO : childCommentDOS) {
            Long userId = childCommentDO.getUserId();
            FindChildCommentItemRspVO childCommentRspVO = FindChildCommentItemRspVO.builder()
                    .userId(userId).commentId(childCommentDO.getId()).imageUrl(childCommentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(childCommentDO.getCreateTime())).likeTotal(childCommentDO.getLikeTotal()).build();

            if (CollUtil.isNotEmpty(userIdAndDTOMap)) {
                FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
                if (Objects.nonNull(findUserByIdRspDTO)) {
                    childCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
                    childCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
                }
                Long replyCommentId = childCommentDO.getReplyCommentId();
                Long parentId = childCommentDO.getParentId();
                if (Objects.nonNull(replyCommentId) && !Objects.equals(replyCommentId, parentId)) {
                    Long replyUserId = childCommentDO.getReplyUserId();
                    FindUserByIdRspDTO replyUser = userIdAndDTOMap.get(replyUserId);
                    childCommentRspVO.setReplyUserName(replyUser.getNickName());
                    childCommentRspVO.setReplyUserId(replyUser.getId());
                }
            }

            if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
                String contentUuid = childCommentDO.getContentUuid();
                if (StringUtils.isNotBlank(contentUuid)) {
                    childCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
                }
            }
            childCommentRspVOS.add(childCommentRspVO);
        }

        threadPoolTaskExecutor.execute(() -> {
            Map<String, String> data = Maps.newHashMap();
            childCommentRspVOS.forEach(commentRspVO -> {
                Long commentId = commentRspVO.getCommentId();
                String key = RedisConstants.buildCommentDetailKey(commentId);
                data.put(key, JsonUtils.toJsonString(commentRspVO));
            });
            batchAddCommentDetailJson2Redis(data);
        });
    }

    private void batchAddCommentDetailJson2Redis(Map<String, String> data) {
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String jsonStr = entry.getValue();
                int randomExpire = 60*60 + RandomUtil.randomInt(4 * 60 * 60);
                connection.setEx(redisTemplate.getStringSerializer().serialize(entry.getKey()), randomExpire, redisTemplate.getStringSerializer().serialize(jsonStr));
            }
            return null;
        });
    }

    private void syncChildComments2Redis(Long parentCommentId, String childCommentZSetKey) {
        List<CommentDO> childCommentDOS = commentDOMapper.selectChildCommentsByParentIdAndLimit(parentCommentId, 6*10);
        if (CollUtil.isNotEmpty(childCommentDOS)) {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
                for (CommentDO childCommentDO : childCommentDOS) {
                    Long commentId = childCommentDO.getId();
                    long commentTimestamp = DateUtils.localDateTime2Timestamp(childCommentDO.getCreateTime());
                    zSetOps.add(childCommentZSetKey, commentId, commentTimestamp);
                }
                int randomExpiryTime = 60*60 + RandomUtil.randomInt(4 * 60 * 60);
                redisTemplate.expire(childCommentZSetKey, randomExpiryTime, TimeUnit.SECONDS);
                return null;
            });
        }
    }

    private void syncCommentCount2Redis(String countCommentKey, Long dbCount) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForHash().put(countCommentKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL, dbCount);
                long expireTime = 60*60 + RandomUtil.randomInt(4*60*60);
                operations.expire(countCommentKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    private void setCommentCountData(List<FindCommentItemRspVO> commentRspVOS, List<Long> expiredCommentIds) {
        List<Long> notExpiredCommentIds = Lists.newArrayList();
        commentRspVOS.forEach(commentRspVO -> {
            Long oneLevelCommentId = commentRspVO.getCommentId();
            notExpiredCommentIds.add(oneLevelCommentId);
            List<FindCommentItemRspVO> childComments = commentRspVO.getChildComments();
            if (CollUtil.isNotEmpty(childComments)) {
                childComments.forEach(childCommentRspVO -> notExpiredCommentIds.add(childCommentRspVO.getCommentId()));
            }
        });

        Map<Long, Map<Object, Object>> commentIdAndCountMap = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);
        for (FindCommentItemRspVO commentRspVO : commentRspVOS) {
            Long commentId = commentRspVO.getCommentId();
            if (CollUtil.isNotEmpty(expiredCommentIds) && expiredCommentIds.contains(commentId)) continue;

            Map<Object, Object> hash = commentIdAndCountMap.get(commentId);
            if (CollUtil.isNotEmpty(hash)) {
                Object childCommentTotalObj = hash.get(RedisConstants.FIELD_CHILD_COMMENT_TOTAL);
                Long childCommentTotal = Objects.isNull(childCommentTotalObj) ? 0 : Long.parseLong(childCommentTotalObj.toString());
                Object likeTotalObj = hash.get(RedisConstants.FIELD_LIKE_TOTAL);
                Long likeTotal = Objects.isNull(likeTotalObj) ? 0 : Long.parseLong(likeTotalObj.toString());
                commentRspVO.setChildCommentTotal(childCommentTotal);
                commentRspVO.setLikeTotal(likeTotal);

                List<FindCommentItemRspVO> childComments = commentRspVO.getChildComments();
                if (CollUtil.isNotEmpty(childComments)) {
                    childComments.forEach(childCommentRspVO -> {
                        Long firstCommentId = childCommentRspVO.getCommentId();
                        Map<Object, Object> firstCommentHash = commentIdAndCountMap.get(firstCommentId);
                        if (CollUtil.isNotEmpty(firstCommentHash)) {
                            Long firstCommentLikeTotal = Long.valueOf(firstCommentHash.get(RedisConstants.FIELD_LIKE_TOTAL).toString());
                            childCommentRspVO.setLikeTotal(firstCommentLikeTotal);
                        }
                    });
                }
            }
        }
    }

    private void syncCommentDetail2LocalCache(List<FindCommentItemRspVO> commentRspVOS) {
        threadPoolTaskExecutor.execute(() -> {
            Map<Long, String> localCacheData = Maps.newHashMap();
            commentRspVOS.forEach(commentRspVO -> {
                Long commentId = commentRspVO.getCommentId();
                localCacheData.put(commentId, JsonUtils.toJsonString(commentRspVO));
            });
            LOCAL_CACHE.putAll(localCacheData);
        });
    }

    private void getCommentDataAndSync2Redis(List<CommentDO> oneLevelCommentDOS, Long noteId, List<FindCommentItemRspVO> commentRspVOS) {
        List<Long> twoLevelCommentIds = oneLevelCommentDOS.stream()
                .map(CommentDO::getFirstReplyCommentId)
                .filter(firstReplyCommentId -> firstReplyCommentId != 0)
                .toList();

        Map<Long, CommentDO> commentIdAndDOMap = null;
        List<CommentDO> twoLevelCommonDOS = null;
        if (CollUtil.isNotEmpty(twoLevelCommentIds)) {
            twoLevelCommonDOS = commentDOMapper.selectTwoLevelCommentByIds(twoLevelCommentIds);
            commentIdAndDOMap = twoLevelCommonDOS.stream().collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
        }

        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        List<Long> userIds = Lists.newArrayList();

        List<CommentDO> allCommentDOS = Lists.newArrayList();
        CollUtil.addAll(allCommentDOS, oneLevelCommentDOS);
        CollUtil.addAll(allCommentDOS, twoLevelCommonDOS);

        allCommentDOS.forEach(commentDO -> {
            boolean isContentEmpty = commentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(commentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            userIds.add(commentDO.getUserId());
        });

        List<FindCommentContentRspDTO> findCommentContentRspDTOS = keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentUuidAndContentMap = findCommentContentRspDTOS.stream().collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
        }

        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);
        Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            userIdAndDTOMap = findUserByIdRspDTOS.stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
        }

        for (CommentDO commentDO : oneLevelCommentDOS) {
            Long userId = commentDO.getUserId();
            FindCommentItemRspVO oneLevelCommentRspVO = FindCommentItemRspVO.builder()
                    .userId(userId).commentId(commentDO.getId()).imageUrl(commentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(commentDO.getCreateTime())).likeTotal(commentDO.getLikeTotal())
                    .childCommentTotal(commentDO.getChildCommentTotal()).heat(commentDO.getHeat()).build();

            setUserInfo(commentIdAndDOMap, userIdAndDTOMap, userId, oneLevelCommentRspVO);
            setCommentContent(commentUuidAndContentMap, commentDO, oneLevelCommentRspVO);

            Long firstReplyCommentId = commentDO.getFirstReplyCommentId();
            if (CollUtil.isNotEmpty(commentIdAndDOMap)) {
                CommentDO firstReplyCommentDO = commentIdAndDOMap.get(firstReplyCommentId);
                if (Objects.nonNull(firstReplyCommentDO)) {
                    Long firstReplyCommentUserId = firstReplyCommentDO.getUserId();
                    FindCommentItemRspVO firstReplyCommentRspVO = FindCommentItemRspVO.builder()
                            .userId(firstReplyCommentDO.getUserId()).commentId(firstReplyCommentDO.getId())
                            .imageUrl(firstReplyCommentDO.getImageUrl()).createTime(DateUtils.formatRelativeTime(firstReplyCommentDO.getCreateTime()))
                            .likeTotal(firstReplyCommentDO.getLikeTotal()).heat(firstReplyCommentDO.getHeat()).build();

                    setUserInfo(commentIdAndDOMap, userIdAndDTOMap, firstReplyCommentUserId, firstReplyCommentRspVO);
                    oneLevelCommentRspVO.setChildComments(Collections.singletonList(firstReplyCommentRspVO));
                    setCommentContent(commentUuidAndContentMap, firstReplyCommentDO, firstReplyCommentRspVO);
                }
            }
            commentRspVOS.add(oneLevelCommentRspVO);
        }

        threadPoolTaskExecutor.execute(() -> {
            Map<String, String> data = Maps.newHashMap();
            commentRspVOS.forEach(commentRspVO -> {
                Long commentId = commentRspVO.getCommentId();
                String key = RedisConstants.buildCommentDetailKey(commentId);
                data.put(key, JsonUtils.toJsonString(commentRspVO));
            });
            batchAddCommentDetailJson2Redis(data);
        });
    }

    private void syncHeatComments2Redis(String key, Long noteId) {
        List<CommentDO> commentDOS = commentDOMapper.selectHeatComments(noteId);
        if (CollUtil.isNotEmpty(commentDOS)) {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
                for (CommentDO commentDO : commentDOS) {
                    Long commentId = commentDO.getId();
                    Double commentHeat = commentDO.getHeat();
                    zSetOps.add(key, commentId, commentHeat);
                }
                int randomExpiryTime = RandomUtil.randomInt(5 * 60 * 60);
                redisTemplate.expire(key, randomExpiryTime, TimeUnit.SECONDS);
                return null;
            });
        }
    }

    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentDO commentDO1, FindCommentItemRspVO firstReplyCommentRspVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentDO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }

    private static void setUserInfo(Map<Long, CommentDO> commentIdAndDOMap, Map<Long, FindUserByIdRspDTO> userIdAndDTOMap, Long userId, FindCommentItemRspVO oneLevelCommentRspVO) {
        FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
        if (Objects.nonNull(findUserByIdRspDTO)) {
            oneLevelCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
            oneLevelCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
        }
    }

    /**
     * 设置一级、及附带的首条二级评论是否被当前用户点赞
     *
     * @param commentRspVOS
     */
    private void setCommentIsLikedData(List<FindCommentItemRspVO> commentRspVOS) {
        Long userId = LoginUserContextHolder.getUserId();
        // 1. 如果未登录，直接将全部评论的 isLiked 置为 false，并且清洗掉可能被缓存污染的脏数据
        if (Objects.isNull(userId)) {
            for (FindCommentItemRspVO vo : commentRspVOS) {
                vo.setIsLiked(false);
                if (CollUtil.isNotEmpty(vo.getChildComments())) {
                    vo.getChildComments().forEach(c -> c.setIsLiked(false));
                }
            }
            return;
        }

        // 2. 提取当前页所有的评论 ID（包含一级和附带的二级评论）
        List<Long> allCommentIds = Lists.newArrayList();
        for (FindCommentItemRspVO vo : commentRspVOS) {
            allCommentIds.add(vo.getCommentId());
            if (CollUtil.isNotEmpty(vo.getChildComments())) {
                vo.getChildComments().forEach(c -> allCommentIds.add(c.getCommentId()));
            }
        }

        if (CollUtil.isEmpty(allCommentIds)) return;

        // 3. 去数据库批量查询当前用户点赞过的评论 ID 集合
        List<Long> likedCommentIds = commentLikeDOMapper.selectLikedCommentIds(userId, allCommentIds);

        // 4. 遍历当前页，将匹配的设为 true，不匹配的设为 false
        for (FindCommentItemRspVO vo : commentRspVOS) {
            vo.setIsLiked(likedCommentIds.contains(vo.getCommentId()));
            if (CollUtil.isNotEmpty(vo.getChildComments())) {
                vo.getChildComments().forEach(c -> c.setIsLiked(likedCommentIds.contains(c.getCommentId())));
            }
        }
    }

    /**
     * 设置二级子评论列表是否被当前用户点赞
     *
     * @param childCommentRspVOS
     */
    private void setChildCommentIsLikedData(List<FindChildCommentItemRspVO> childCommentRspVOS) {
        Long userId = LoginUserContextHolder.getUserId();
        // 1. 未登录，全部设为 false
        if (Objects.isNull(userId)) {
            childCommentRspVOS.forEach(vo -> vo.setIsLiked(false));
            return;
        }

        // 2. 提取子评论 ID
        List<Long> allCommentIds = childCommentRspVOS.stream()
                .map(FindChildCommentItemRspVO::getCommentId)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(allCommentIds)) return;

        // 3. 批量查询
        List<Long> likedCommentIds = commentLikeDOMapper.selectLikedCommentIds(userId, allCommentIds);

        // 4. 判断并赋值
        childCommentRspVOS.forEach(vo -> vo.setIsLiked(likedCommentIds.contains(vo.getCommentId())));
    }

    @Override
    public Response<FindCommentByIdRspDTO> getNoteIdByCommentId(LikeCommentReqDTO vo) {
        FindCommentByIdRspDTO dto = commentDOMapper.getNoteIdByCommentId(vo.getCommentId());
        return Response.success(dto);
    }
}