package com.wsz.xiaolanshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.wsz.framework.common.response.Response;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.note.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.ChannelDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindChannelRspVO;
import com.wsz.xiaolanshu.note.biz.mapper.ChannelDOMapper;
import com.wsz.xiaolanshu.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 频道业务实现
 */
@Service
@Slf4j
public class ChannelServiceImpl implements ChannelService {

    @Resource
    private ChannelDOMapper channelDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地一级缓存 (Caffeine)
     * 频道列表全站公用，设置 10 分钟过期
     */
    private static final Cache<String, List<FindChannelRspVO>> CHANNEL_LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1)
            .maximumSize(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * 查询所有频道 (带多级缓存)
     *
     * @return
     */
    @Override
    public Response<List<FindChannelRspVO>> findChannelList() {
        String cacheKey = RedisConstants.CHANNEL_LIST_KEY;

        // 1. 尝试从本地缓存获取
        List<FindChannelRspVO> localCacheData = CHANNEL_LOCAL_CACHE.getIfPresent(cacheKey);
        if (CollUtil.isNotEmpty(localCacheData)) {
            log.info("==> 命中本地缓存: {}", cacheKey);
            return Response.success(localCacheData);
        }

        // 2. 尝试从 Redis 获取
        Object redisData = redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(redisData)) {
            log.info("==> 命中 Redis 缓存: {}", cacheKey);
            // 将 JSON 字符串还原为 List 集合
            List<FindChannelRspVO> redisCacheData = null;
            try {
                redisCacheData = JsonUtils.parseList(redisData.toString(), FindChannelRspVO.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (CollUtil.isNotEmpty(redisCacheData)) {
                // 回填本地缓存
                CHANNEL_LOCAL_CACHE.put(cacheKey, redisCacheData);
                return Response.success(redisCacheData);
            }
        }

        // 3. 缓存均未命中，查询数据库
        log.info("==> 缓存未命中，查询数据库: {}", cacheKey);
        List<ChannelDO> channelDOS = channelDOMapper.selectAll();

        List<FindChannelRspVO> channelRspVOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(channelDOS)) {
            channelRspVOS = channelDOS.stream()
                    .map(channelDO -> FindChannelRspVO.builder()
                            .id(channelDO.getId())
                            .name(channelDO.getName())
                            .build())
                    .collect(Collectors.toList());

            // 4. 将数据同步到 Redis 和 本地缓存
            // Redis 设置 1 小时过期，防止缓存过期导致突发流量打到 DB
            redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJsonString(channelRspVOS), 1, TimeUnit.HOURS);
            CHANNEL_LOCAL_CACHE.put(cacheKey, channelRspVOS);
        }

        return Response.success(channelRspVOS);
    }
}