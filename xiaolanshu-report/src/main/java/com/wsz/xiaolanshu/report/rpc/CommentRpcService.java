package com.wsz.xiaolanshu.report.rpc;

import com.wsz.xiaolanshu.comment.api.CommentFeignApi;
import com.wsz.xiaolanshu.comment.dto.DeleteCommentReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-21 19:35
 * @Company:
 */
@Component
public class CommentRpcService {

    @Resource
    private CommentFeignApi commentFeignApi;

    public void deleteComment(Long targetId) {
        DeleteCommentReqVO vo = new DeleteCommentReqVO();
        vo.setCommentId(targetId);

        commentFeignApi.deleteComment(vo);
    }
}
