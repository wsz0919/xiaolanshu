package com.wsz.xiaolanshu.note.biz.listener;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.count.dto.FindUserCountsByIdRspDTO;
import com.wsz.xiaolanshu.note.biz.constant.MQConstants;
import com.wsz.xiaolanshu.note.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.note.biz.convert.NoteConvert;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.NoteDO;
import com.wsz.xiaolanshu.note.biz.domain.dto.NoteOperateMqDTO;
import com.wsz.xiaolanshu.note.biz.domain.dto.PublishNoteDTO;
import com.wsz.xiaolanshu.note.biz.enums.NoteOperateEnum;
import com.wsz.xiaolanshu.note.biz.mapper.NoteDOMapper;
import com.wsz.xiaolanshu.note.biz.rpc.CountRpcService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 17:31
 * @Company:
 */
@RocketMQTransactionListener
@Slf4j
public class PublishNote2DBLocalTransactionListener implements RocketMQLocalTransactionListener {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private NoteDOMapper noteDOMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private CountRpcService countRpcService;

    /**
     * 定义大V粉丝数阈值 (这里可抽取至分布式配置中心)
     */
    private static final long BIG_V_FANS_THRESHOLD = 10000L;

    /**
     * 执行本地事务
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始执行本地事务：{}", payload);

        // 消息体 Json 转 DTO
        PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(payload, PublishNoteDTO.class);

        Long noteId = publishNoteDTO.getId();
        Long creatorId = publishNoteDTO.getCreatorId();

        // 个人主页 - 已发布笔记列表缓存 Key
        String publishedNoteListRedisKey = RedisConstants.buildPublishedNoteListKey(creatorId);

        // RPC: 获取用户统计信息，判断是否为大V
        FindUserCountsByIdRspDTO userCounts = countRpcService.findUserCountsById(creatorId);
        boolean isBigV = Objects.nonNull(userCounts)
                && Objects.nonNull(userCounts.getFansTotal())
                && userCounts.getFansTotal() >= BIG_V_FANS_THRESHOLD;

        // 【针对大V采取灵活策略：大V更新缓存，普通用户删除缓存
        if (isBigV) {
            // 大V: 直接更新缓存，将新发布的笔记追加到 ZSet 列表头部（Score 为当前时间戳）
            // 注意: 只在缓存本身已存在时追加，避免缓存未预热导致创建脏数据
            if (Boolean.TRUE.equals(redisTemplate.hasKey(publishedNoteListRedisKey))) {
                redisTemplate.opsForZSet().add(publishedNoteListRedisKey, String.valueOf(noteId), System.currentTimeMillis());
                log.info("## 发现大V发布笔记，直接更新 Redis 缓存...");
            }
        } else {
            // 普通用户: 直接删除缓存，等待下一次用户拉取时重建
            redisTemplate.delete(publishedNoteListRedisKey);
        }

        // 2. 执行本地事务（如数据库操作）
        try {
            // DTO 转 DO
            NoteDO noteDO = NoteConvert.INSTANCE.convertDTO2DO(publishNoteDTO);

            // 笔记元数据写库
            noteDOMapper.insert(noteDO);
        } catch (Exception e) {
            log.error("## 笔记元数据存储失败: ", e);
            return RocketMQLocalTransactionState.ROLLBACK; // 回滚事务消息
        }

        // 延迟双删：发送延迟消息
        // 大V是更新策略，且前面已直接对 ZSet 进行追加操作，不需要延迟删除，否则会把辛苦追加的缓存删掉
        // 因此延迟双删只针对普通用户生效
        if (!isBigV) {
            sendDelayDeleteRedisPublishedNoteListCacheMQ(creatorId);
        }

        // 发送 MQ
        // 构建消息体 DTO
        NoteOperateMqDTO noteOperateMqDTO = NoteOperateMqDTO.builder()
                .creatorId(creatorId)
                .noteId(noteId)
                .type(NoteOperateEnum.PUBLISH.getCode()) // 发布笔记
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(noteOperateMqDTO))
                .build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_NOTE_OPERATE + ":" + MQConstants.TAG_NOTE_PUBLISH;

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记发布】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记发布】MQ 发送异常: ", throwable);
            }
        });

        // 3. 提交事务状态，“half 消息” 转换为正式消息
        return RocketMQLocalTransactionState.COMMIT;
    }

    private void sendDelayDeleteRedisPublishedNoteListCacheMQ(Long userId) {
        Message<String> message = MessageBuilder.withPayload(String.valueOf(userId))
                .build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 已发布笔记列表缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 已发布笔记列表缓存消息发送失败...", e);
                    }
                },
                3000, // 超时时间
                1 // 延迟级别，1 表示延时 1s (注意：RocketMQ 的延迟级别 1为1s, 2为5s, 3为10s等)
        );
    }

    /**
     * 事务状态回查（由 Broker 主动调用）
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始事务回查：{}", payload);

        // 消息体 Json 转 DTO
        PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(payload, PublishNoteDTO.class);

        Long noteId = publishNoteDTO.getId();

        // 2. 检查本地事务状态（若记录存在，说明本地事务执行成功了；否则执行失败）
        int count = noteDOMapper.selectCountByNoteId(noteId);

        // 3. 返回最终状态
        return count == 1 ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.ROLLBACK;
    }
}