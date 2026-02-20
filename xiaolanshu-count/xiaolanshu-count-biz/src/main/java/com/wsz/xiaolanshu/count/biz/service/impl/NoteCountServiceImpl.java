package com.wsz.xiaolanshu.count.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.count.biz.domain.dataobject.NoteCountDO;
import com.wsz.xiaolanshu.count.biz.mapper.NoteCountDOMapper;
import com.wsz.xiaolanshu.count.biz.service.NoteCountService;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdRspDTO;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:37
 * @Company:
 */
@Service
@Slf4j
public class NoteCountServiceImpl implements NoteCountService {

    @Resource
    private NoteCountDOMapper noteCountDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Response<FindNoteCountByIdRspDTO> findNoteCountData(FindNoteCountByIdReqDTO findNoteCountByIdReqDTO) {
        Long noteId = findNoteCountByIdReqDTO.getNoteId();

        // 构建 Redis Key
        String noteCountKey = RedisConstants.buildCountNoteKey(noteId);

        // 1. 先从 Redis 中查询
        List<Object> countValues = redisTemplate.opsForHash().multiGet(noteCountKey, List.of(
                RedisConstants.FIELD_LIKE_TOTAL,
                RedisConstants.FIELD_COLLECT_TOTAL,
                RedisConstants.FIELD_COMMENT_TOTAL
        ));

        // 提取计数
        Object likeTotalObj = countValues.get(0);
        Object collectTotalObj = countValues.get(1);
        Object commentTotalObj = countValues.get(2);

        // 如果缓存中所有数据都存在，直接返回
        if (Objects.nonNull(likeTotalObj) && Objects.nonNull(collectTotalObj) && Objects.nonNull(commentTotalObj)) {
            FindNoteCountByIdRspDTO findNoteCountByIdRspDTO = FindNoteCountByIdRspDTO.builder()
                    .noteId(noteId)
                    .likeTotal(Long.valueOf(likeTotalObj.toString()))
                    .collectTotal(Long.valueOf(collectTotalObj.toString()))
                    .commentTotal(Long.valueOf(commentTotalObj.toString()))
                    .build();
            return Response.success(findNoteCountByIdRspDTO);
        }

        // 2. 缓存中不存在，查询数据库
        NoteCountDO noteCountDO = noteCountDOMapper.selectByNoteId(noteId);

        FindNoteCountByIdRspDTO findNoteCountByIdRspDTO = FindNoteCountByIdRspDTO.builder()
                .noteId(noteId)
                .collectTotal(0L)
                .commentTotal(0L)
                .likeTotal(0L)
                .build();

        if (Objects.nonNull(noteCountDO)) {
            findNoteCountByIdRspDTO.setCollectTotal(noteCountDO.getCollectTotal());
            findNoteCountByIdRspDTO.setCommentTotal(noteCountDO.getCommentTotal());
            findNoteCountByIdRspDTO.setLikeTotal(noteCountDO.getLikeTotal());

            // 3. 同步到 Redis
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) {
                    Map<String, Long> countMap = Maps.newHashMap();
                    countMap.put(RedisConstants.FIELD_LIKE_TOTAL, noteCountDO.getLikeTotal());
                    countMap.put(RedisConstants.FIELD_COLLECT_TOTAL, noteCountDO.getCollectTotal());
                    countMap.put(RedisConstants.FIELD_COMMENT_TOTAL, noteCountDO.getCommentTotal());

                    operations.opsForHash().putAll(noteCountKey, countMap);

                    // 设置随机过期时间 (1小时以内)
                    long expireTime = 60 * 30 + RandomUtil.randomInt(60 * 30);
                    operations.expire(noteCountKey, expireTime, TimeUnit.SECONDS);
                    return null;
                }
            });
        }

        return Response.success(findNoteCountByIdRspDTO);
    }

}
