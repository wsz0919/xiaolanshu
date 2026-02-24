package com.wsz.xiaolanshu.user.biz.rpc;

import com.wsz.xiaolanshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-10 16:48
 * @Company:
 */
@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * Leaf 号段模式：小哈书 ID 业务标识
     */
    private static final String BIZ_TAG_XIAOLANSHU_ID = "leaf-segment-xiaolanshu-id";

    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";

    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_NOTICE_ID = "leaf-segment-notice-id";

    /**
     * 调用分布式 ID 生成服务生成小哈书 ID
     *
     * @return
     */
    public String getXiaohashuId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_XIAOLANSHU_ID);
    }

    /**
     * 调用分布式 ID 生成服务用户 ID
     *
     * @return
     */
    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }

    /**
     * 调用分布式 ID 生成服务用户 ID
     *
     * @return
     */
    public String getNoticeId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_NOTICE_ID);
    }
}
