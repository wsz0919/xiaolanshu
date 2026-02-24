package com.wsz.xiaolanshu.notice.biz.consumer;

import com.wsz.framework.common.util.JsonUtils;
import com.wsz.xiaolanshu.count.biz.domain.dto.CountLikeUnlikeNoteMqDTO;
import com.wsz.xiaolanshu.count.biz.enums.LikeUnlikeNoteTypeEnum;
import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import com.wsz.xiaolanshu.notice.biz.constant.MQConstants;
import com.wsz.xiaolanshu.notice.biz.domain.dataobject.NoticeDO;
import com.wsz.xiaolanshu.notice.biz.mapper.NoticeDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-24 15:30
 * @Company:
 */
@Component
@RocketMQMessageListener(
        consumerGroup = "xiaolanshu_notice_group_" + MQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE
)
@Slf4j
public class NoticeNoteLikeConsumer implements RocketMQListener<String> {

    @Resource
    private NoticeDOMapper noticeDOMapper;
    @Resource
    private DistributedIdGeneratorFeignApi idGeneratorApi;

    @Override
    public void onMessage(String body) {
        log.info("==> NoticeNoteLikeConsumer 消费了消息: {}", body);

        CountLikeUnlikeNoteMqDTO dto = JsonUtils.parseObject(body, CountLikeUnlikeNoteMqDTO.class);
        if (Objects.isNull(dto)) return;

        // 如果是取消点赞，直接忽略不发通知
        LikeUnlikeNoteTypeEnum typeEnum = LikeUnlikeNoteTypeEnum.valueOf(dto.getType());
        if (Objects.isNull(typeEnum) || typeEnum == LikeUnlikeNoteTypeEnum.UNLIKE) return;

        // 自己给自己点赞，不发通知
        if (dto.getUserId().equals(dto.getNoteCreatorId())) return;

        NoticeDO notice = new NoticeDO();
        notice.setId(Long.valueOf(idGeneratorApi.getSegmentId("leaf-segment-notice-id")));
        notice.setReceiverId(dto.getNoteCreatorId()); // DTO里直接有作者ID，省去了RPC调用笔记服务查作者！
        notice.setSenderId(dto.getUserId());
        notice.setType(1); // 1-赞和收藏
        notice.setSubType(11); // 11-点赞笔记
        notice.setTargetId(dto.getNoteId());
        noticeDOMapper.insert(notice);
    }
}
