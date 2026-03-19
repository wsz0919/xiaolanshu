package com.wsz.xiaolanshu.note.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.kv.dto.resp.FindNoteContentRspDTO;
import com.wsz.xiaolanshu.note.biz.constant.MQConstants;
import com.wsz.xiaolanshu.note.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminNoteRspVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.AdminUpdateNoteStatusReqVO;
import com.wsz.xiaolanshu.note.biz.domain.vo.FindNotePageListReqVO;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.rpc.KeyValueRpcService;
import com.wsz.xiaolanshu.note.biz.rpc.UserRpcService;
import com.wsz.xiaolanshu.note.biz.service.AdminNoteService;
import com.wsz.xiaolanshu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private KeyValueRpcService keyValueRpcService;

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // 构建 Caffeine 一级缓存 (本地内存)
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .initialCapacity(100)      // 初始容量
            .maximumSize(5000)         // 最大容量，超过后会基于 LRU 淘汰
            .expireAfterWrite(5, TimeUnit.MINUTES) // L1 缓存过期时间 (建议比 Redis 短)
            .build();

    @Override
    public PageResponse<AdminNoteRspVO> findNotePageList(FindNotePageListReqVO reqVO) {
        long pageNo = reqVO.getPageNo();
        long pageSize = reqVO.getPageSize();
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 1. 查询符合条件的总数据量
        long totalCount = noteDOMapper.selectAdminTotalCount(reqVO.getTitle(), reqVO.getCreatorId(), reqVO.getStatus());
        List<AdminNoteRspVO> vos = new ArrayList<>();

        if (totalCount > 0) {
            // 2. 查询当前页数据
            List<NoteDO> noteDOList = noteDOMapper.selectAdminPageList(
                    reqVO.getTitle(),
                    reqVO.getCreatorId(),
                    reqVO.getStatus(),
                    offset,
                    pageSize
            );

            // 3. 提取去重后的 ID 集合 (过滤掉 null 和 空字符串)
            List<Long> creatorIds = noteDOList.stream()
                    .map(NoteDO::getCreatorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> contentUuids = noteDOList.stream()
                    .map(NoteDO::getContentUuid)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());

            // 4. 使用 CompletableFuture 并发获取 (多级缓存 + 批量兜底)
            CompletableFuture<Map<Long, String>> userMapFuture = CompletableFuture.supplyAsync(
                    () -> batchGetUserNickNameMap(creatorIds), threadPoolTaskExecutor
            );

            CompletableFuture<Map<String, String>> noteContentMapFuture = CompletableFuture.supplyAsync(
                    () -> batchGetNoteContentMap(contentUuids), threadPoolTaskExecutor
            );

            // 等待并发任务全部执行完毕
            CompletableFuture.allOf(userMapFuture, noteContentMapFuture).join();

            try {
                // 提取并发执行的结果
                Map<Long, String> userNickNameMap = userMapFuture.get();
                Map<String, String> noteContentMap = noteContentMapFuture.get();

                // 5. DO 转换 为 VO
                vos = noteDOList.stream()
                        .map(noteDO -> AdminNoteRspVO.builder()
                                .id(noteDO.getId())
                                .title(noteDO.getTitle())
                                .creatorId(noteDO.getCreatorId())
                                .type(noteDO.getType())
                                .nickName(userNickNameMap.getOrDefault(noteDO.getCreatorId(), "未知用户"))
                                .cover(StringUtils.isNotBlank(noteDO.getImgUris()) ? noteDO.getImgUris() : noteDO.getVideoUri())
                                .content(noteContentMap.getOrDefault(noteDO.getContentUuid(), ""))
                                .status(noteDO.getStatus())
                                .visible(noteDO.getVisible())
                                .createTime(noteDO.getCreateTime())
                                .build())
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.error("并发获取笔记扩展信息失败", e);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        }

        return PageResponse.success(vos, pageNo, totalCount, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 建议加上事务
    public Response<?> updateNoteStatus(AdminUpdateNoteStatusReqVO reqVO) {

        // 1. 先查询原笔记信息 (为了拿到 creatorId 用于清理该用户的列表缓存)
        NoteDO oldNote = noteDOMapper.selectByPrimaryToKey(reqVO.getId());
        if (oldNote == null) {
            return Response.fail("笔记不存在");
        }

        // 2. 封装 DO 进行状态更新
        NoteDO noteDO = NoteDO.builder()
                .id(reqVO.getId())
                .status(reqVO.getStatus())
                .updateTime(LocalDateTime.now())
                .build();

        // 3. 更新数据库
        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);
        if (count == 0) {
            log.warn("笔记审核状态更新失败，笔记不存在. ID: {}", reqVO.getId());
            return Response.fail("笔记不存在或更新失败");
        }

        // 4. ========== 核心：清理缓存联动 ==========
        // 假设您的状态码中，审核拒绝或下架的状态为 2 或 3 (请替换为您实际的 NoteStatusEnum)
        // 只要不是正常状态，我们就应该把 C 端的可见缓存清掉
        if (reqVO.getStatus() != 1) { // 假设 1 是正常发布状态

            Long noteId = reqVO.getId();
            Long creatorId = oldNote.getCreatorId();

            // 4.1 删除 Redis 中的笔记详情缓存
            // 这里的 Key 请替换为您项目 RedisConstants 里定义的真实 Key
            String noteDetailCacheKey = RedisConstants.NOTE_DETAIL_KEY + noteId;
            redisTemplate.delete(noteDetailCacheKey);

            // 4.2 删除 Redis 中该创作者主页的【已发布笔记列表】缓存
            // String userPublishedListKey = RedisConstants.USER_PUBLISHED_NOTE_LIST_KEY_PREFIX + creatorId;
            // redisTemplate.delete(userPublishedListKey);

            // 4.3 发送 MQ 消息：广播通知所有节点清理 Caffeine 本地缓存
            // 对应您项目里的 DeleteNoteLocalCacheConsumer 逻辑
            rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);

            // 4.4 延迟双删 (可选，对应您项目中的 DelayDeleteNoteRedisCacheConsumer)
            // 为了防止并发读写导致的缓存不一致，发送延迟 MQ 再次删除 Redis 缓存
            List<Long> payload = Arrays.asList(noteId, creatorId);
            Message<List<Long>> delayMsg = MessageBuilder.withPayload(payload).build();
            rocketMQTemplate.syncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, delayMsg, 1000, 3);

            log.info("笔记 ID: {} 已下架，相关 Redis 缓存及本地缓存清理 MQ 已发送", noteId);
        }

        return Response.success();
    }

    /**
     * 批量获取用户昵称 (多级缓存 L1 -> L2 -> 批量 RPC 兜底)
     */
    private Map<Long, String> batchGetUserNickNameMap(List<Long> creatorIds) {
        Map<Long, String> resultMap = new HashMap<>();
        if (creatorIds == null || creatorIds.isEmpty()) {
            return resultMap;
        }

        List<Long> missingIds = new ArrayList<>();

        // Step 1 & 2: 尝试从 Caffeine (L1) 和 Redis (L2) 获取
        for (Long id : creatorIds) {
            String cacheKey = "admin:user:nickname:" + id;

            // 查 L1
            String nickName = localCache.getIfPresent(cacheKey);
            if (StringUtils.isNotBlank(nickName)) {
                resultMap.put(id, nickName);
                continue;
            }

            // 查 L2
            nickName = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotBlank(nickName)) {
                localCache.put(cacheKey, nickName); // 回写 L1
                resultMap.put(id, nickName);
                continue;
            }

            // 两层缓存都没命中，记录为缺页 ID
            missingIds.add(id);
        }

        // Step 3: 对缺页的 ID 发起一次批量 RPC 调用
        if (!missingIds.isEmpty()) {
            List<FindUserByIdRspDTO> rpcResult = userRpcService.findByIds(missingIds);

            if (rpcResult != null) {
                for (FindUserByIdRspDTO user : rpcResult) {
                    Long id = user.getId();
                    String name = StringUtils.isNotBlank(user.getNickName()) ? user.getNickName() : "未知用户";

                    resultMap.put(id, name);
                    missingIds.remove(id); // 从缺页列表中移除已找到的

                    // 结果依次写回 L2 和 L1
                    String cacheKey = "admin:user:nickname:" + id;
                    redisTemplate.opsForValue().set(cacheKey, name, 1, TimeUnit.HOURS);
                    localCache.put(cacheKey, name);
                }
            }

            // Step 4: 防止缓存穿透的兜底处理（底层库也没有查到数据）
            for (Long missingId : missingIds) {
                resultMap.put(missingId, "未知用户");
                String cacheKey = "admin:user:nickname:" + missingId;
                redisTemplate.opsForValue().set(cacheKey, "未知用户", 5, TimeUnit.MINUTES);
                localCache.put(cacheKey, "未知用户");
            }
        }

        return resultMap;
    }

    /**
     * 批量获取笔记内容 (多级缓存 L1 -> L2 -> 批量 RPC 兜底)
     */
    private Map<String, String> batchGetNoteContentMap(List<String> uuids) {
        Map<String, String> resultMap = new HashMap<>();
        if (uuids == null || uuids.isEmpty()) {
            return resultMap;
        }

        List<String> missingUuids = new ArrayList<>();

        // Step 1 & 2: 尝试从 L1 和 L2 获取
        for (String uuid : uuids) {
            String cacheKey = "admin:note:content:" + uuid;

            String content = localCache.getIfPresent(cacheKey);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put(uuid, content);
                continue;
            }

            content = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotBlank(content)) {
                localCache.put(cacheKey, content);
                resultMap.put(uuid, content);
                continue;
            }

            missingUuids.add(uuid);
        }

        // Step 3: 调用批量 RPC 获取缺失的笔记内容
        if (!missingUuids.isEmpty()) {
            List<FindNoteContentRspDTO> rpcResult = keyValueRpcService.findNoteContentBatch(missingUuids);

            if (rpcResult != null) {
                for (FindNoteContentRspDTO dto : rpcResult) {
                    String uuid = String.valueOf(dto.getUuid());
                    String content = dto.getContent() != null ? dto.getContent() : "";

                    resultMap.put(uuid, content);
                    missingUuids.remove(uuid);

                    // 笔记内容较冷且不常修改，写回缓存可设置 24 小时
                    String cacheKey = "admin:note:content:" + uuid;
                    redisTemplate.opsForValue().set(cacheKey, content, 24, TimeUnit.HOURS);
                    localCache.put(cacheKey, content);
                }
            }

            // Step 4: 兜底处理 KV 中彻底找不到的脏数据，防止缓存穿透
            for (String missingUuid : missingUuids) {
                resultMap.put(missingUuid, "");
                String cacheKey = "admin:note:content:" + missingUuid;
                redisTemplate.opsForValue().set(cacheKey, "", 5, TimeUnit.MINUTES);
                localCache.put(cacheKey, "");
            }
        }

        return resultMap;
    }

}
