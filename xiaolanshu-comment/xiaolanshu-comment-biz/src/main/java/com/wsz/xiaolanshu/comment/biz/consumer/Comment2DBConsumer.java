package com.wsz.xiaolanshu.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.comment.biz.constant.MQConstants;
import com.wsz.xiaolanshu.comment.biz.constant.RedisConstants;
import com.wsz.xiaolanshu.comment.biz.domain.bo.CommentBO;
import com.wsz.xiaolanshu.comment.biz.domain.dataobject.CommentDO;
import com.wsz.xiaolanshu.comment.biz.domain.dto.PublishCommentMqDTO;
import com.wsz.xiaolanshu.comment.biz.enums.CommentLevelEnum;
import com.wsz.xiaolanshu.comment.biz.mapper.CommentDOMapper;
import com.wsz.xiaolanshu.comment.biz.mapper.NoteCountDOMapper;
import com.wsz.xiaolanshu.comment.biz.rpc.KeyValueRpcService;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Comment2DBConsumer {

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    private DefaultMQPushConsumer consumer;

    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Resource
    private CommentDOMapper commentDOMapper;

    @Resource
    private NoteCountDOMapper noteCountDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private KeyValueRpcService keyValueRpcService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public DefaultMQPushConsumer mqPushConsumer() throws MQClientException {
        String group = "xiaolanshu_group_" + MQConstants.TOPIC_PUBLISH_COMMENT;
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(MQConstants.TOPIC_PUBLISH_COMMENT, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setConsumeMessageBatchMaxSize(30);

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            try {
                rateLimiter.acquire();

                List<PublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String msgJson = new String(msg.getBody());
                    publishCommentMqDTOS.add(JsonUtils.parseObject(msgJson, PublishCommentMqDTO.class));
                });

                List<Long> replyCommentIds = publishCommentMqDTOS.stream()
                        .filter(dto -> Objects.nonNull(dto.getReplyCommentId()))
                        .map(PublishCommentMqDTO::getReplyCommentId).toList();

                List<CommentDO> replyCommentDOS = null;
                if (CollUtil.isNotEmpty(replyCommentIds)) {
                    replyCommentDOS = commentDOMapper.selectByCommentIds(replyCommentIds);
                }

                Map<Long, CommentDO> commentIdAndCommentDOMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(replyCommentDOS)) {
                    commentIdAndCommentDOMap = replyCommentDOS.stream().collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
                }

                List<CommentBO> commentBOS = Lists.newArrayList();
                for (PublishCommentMqDTO dto : publishCommentMqDTOS) {
                    String imageUrl = dto.getImageUrl();
                    CommentBO commentBO = CommentBO.builder()
                            .id(dto.getCommentId())
                            .noteId(dto.getNoteId())
                            .userId(dto.getCreatorId())
                            .isContentEmpty(true)
                            .imageUrl(StringUtils.isBlank(imageUrl) ? "" : imageUrl)
                            .level(CommentLevelEnum.ONE.getCode())
                            .parentId(dto.getNoteId())
                            .createTime(dto.getCreateTime())
                            .updateTime(dto.getCreateTime())
                            .isTop(false)
                            .replyTotal(0L)
                            .likeTotal(0L)
                            .replyCommentId(0L)
                            .replyUserId(0L)
                            .build();

                    String content = dto.getContent();
                    if (StringUtils.isNotBlank(content)) {
                        commentBO.setContentUuid(UUID.randomUUID().toString());
                        commentBO.setIsContentEmpty(false);
                        commentBO.setContent(content);
                    }

                    Long replyCommentId = dto.getReplyCommentId();
                    if (Objects.nonNull(replyCommentId)) {
                        CommentDO replyCommentDO = commentIdAndCommentDOMap.get(replyCommentId);
                        if (Objects.nonNull(replyCommentDO)) {
                            commentBO.setLevel(CommentLevelEnum.TWO.getCode());
                            commentBO.setReplyCommentId(dto.getReplyCommentId());
                            commentBO.setParentId(replyCommentDO.getId());
                            if (Objects.equals(replyCommentDO.getLevel(), CommentLevelEnum.TWO.getCode())) {
                                commentBO.setParentId(replyCommentDO.getParentId());
                            }
                            commentBO.setReplyUserId(replyCommentDO.getUserId());
                        }
                    }
                    commentBOS.add(commentBO);
                }

                Map<Long, Long> noteIdCountMap = commentBOS.stream()
                        .collect(Collectors.groupingBy(CommentBO::getNoteId, Collectors.counting()));

                Integer insertedRows = transactionTemplate.execute(status -> {
                    try {
                        Integer count = commentDOMapper.batchInsert(commentBOS);

                        List<CommentBO> contentNotEmptyBOS = commentBOS.stream()
                                .filter(bo -> Boolean.FALSE.equals(bo.getIsContentEmpty())).toList();
                        if (CollUtil.isNotEmpty(contentNotEmptyBOS)) {
                            keyValueRpcService.batchSaveCommentContent(contentNotEmptyBOS);
                        }

                        for (Map.Entry<Long, Long> entry : noteIdCountMap.entrySet()) {
                            noteCountDOMapper.insertOrUpdateCommentTotalByNoteId(entry.getKey(), entry.getValue().intValue());
                        }
                        return count;
                    } catch (Exception ex) {
                        status.setRollbackOnly();
                        log.error("Batch insert comment error", ex);
                        throw ex;
                    }
                });

                if (Objects.nonNull(insertedRows) && insertedRows > 0) {
                    syncOneLevelComment2RedisZSet(commentBOS);
                    updateChildCommentTotal(commentBOS);
                    updateFirstReplyCommentId(commentBOS);

                    for (Map.Entry<Long, Long> entry : noteIdCountMap.entrySet()) {
                        String noteCommentTotalKey = RedisConstants.buildNoteCommentTotalKey(entry.getKey());
                        if (Boolean.TRUE.equals(redisTemplate.hasKey(noteCommentTotalKey))) {
                            redisTemplate.opsForHash().increment(noteCommentTotalKey, RedisConstants.FIELD_COMMENT_TOTAL, entry.getValue().intValue());
                        }
                    }
                    deleteTwoLevelCommentRedisCache(commentBOS);
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("Consume comment message failed", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });

        consumer.start();
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try { consumer.shutdown(); } catch (Exception e) { log.error("Shutdown consumer error", e); }
        }
    }

    private void updateChildCommentTotal(List<CommentBO> commentBOS) {
        Map<Long, Long> parentCountMap = commentBOS.stream()
                .filter(bo -> Objects.equals(bo.getLevel(), CommentLevelEnum.TWO.getCode()))
                .collect(Collectors.groupingBy(CommentBO::getParentId, Collectors.counting()));

        if (CollUtil.isEmpty(parentCountMap)) return;

        for (Map.Entry<Long, Long> entry : parentCountMap.entrySet()) {
            Long parentId = entry.getKey();
            int count = entry.getValue().intValue();
            commentDOMapper.updateChildCommentTotalById(parentId, count);

            String redisKey = RedisConstants.buildCountCommentKey(parentId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                redisTemplate.opsForHash().increment(redisKey, RedisConstants.FIELD_CHILD_COMMENT_TOTAL, count);
            }
        }
    }

    private void updateFirstReplyCommentId(List<CommentBO> commentBOS) {
        List<Long> parentIds = commentBOS.stream()
                .filter(commentBO -> Objects.equals(commentBO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .map(CommentBO::getParentId).distinct().toList();

        if (CollUtil.isEmpty(parentIds)) return;

        List<String> keys = parentIds.stream().map(RedisConstants::buildHaveFirstReplyCommentKey).toList();
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        List<Long> missingCommentIds = Lists.newArrayList();
        for (int i = 0; i < values.size(); i++) {
            if (Objects.isNull(values.get(i))) missingCommentIds.add(parentIds.get(i));
        }

        if (CollUtil.isNotEmpty(missingCommentIds)) {
            List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(missingCommentIds);
            List<CommentDO> needUpdateDOS = commentDOS.stream().filter(doObj -> doObj.getFirstReplyCommentId() == 0).toList();

            for (CommentDO needUpdateDO : needUpdateDOS) {
                Long parentId = needUpdateDO.getId();
                CommentDO earliest = commentDOMapper.selectEarliestByParentId(parentId);
                if (Objects.nonNull(earliest)) {
                    commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliest.getId(), parentId);
                    redisTemplate.opsForValue().set(RedisConstants.buildHaveFirstReplyCommentKey(parentId), "1",
                            RandomUtil.randomInt(5 * 60 * 60), TimeUnit.SECONDS);
                }
            }
        }
    }

    private void syncOneLevelComment2RedisZSet(List<CommentBO> commentBOS) {
        Map<Long, List<CommentBO>> noteIdGroupMap = commentBOS.stream()
                .filter(bo -> Objects.equals(bo.getLevel(), CommentLevelEnum.ONE.getCode()))
                .collect(Collectors.groupingBy(CommentBO::getNoteId));

        noteIdGroupMap.forEach((noteId, boList) -> {
            String key = RedisConstants.buildCommentListKey(noteId);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_hot_comments.lua")));
            script.setResultType(Long.class);

            // ================== 【核心修复：手动将参数转为 String 类型】 ==================
            List<String> args = Lists.newArrayList();
            boList.forEach(bo -> {
                args.add(String.valueOf(bo.getId())); // 关键：转 String
                args.add("0");                        // 关键：转 String
            });

            try {
                redisTemplate.execute(script, Collections.singletonList(key), args.toArray());
            } catch (Exception e) {
                log.error("## Sync hot comments to redis error, noteId: {}", noteId, e);
            }
        });
    }

    private void deleteTwoLevelCommentRedisCache(List<CommentBO> commentBOS) {
        List<CommentBO> twoLevelComments = commentBOS.stream()
                .filter(bo -> Objects.equals(bo.getLevel(), CommentLevelEnum.TWO.getCode())).toList();

        if (CollUtil.isNotEmpty(twoLevelComments)) {
            Set<Long> parentCommentIds = twoLevelComments.stream().map(CommentBO::getParentId).collect(Collectors.toSet());

            List<String> keysToDelete = Lists.newArrayList();
            parentCommentIds.forEach(pid -> {
                keysToDelete.add(RedisConstants.buildChildCommentListKey(pid));
                keysToDelete.add(RedisConstants.buildCommentDetailKey(pid));

                rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, pid, new SendCallback() {
                    @Override public void onSuccess(SendResult res) {}
                    @Override public void onException(Throwable ex) {}
                });
            });
            redisTemplate.delete(keysToDelete);
        }
    }
}