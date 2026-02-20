package com.wsz.xiaolanshu.comment.biz.rpc;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-20 12:29
 * @Company:
 */
@Component
@Slf4j
public class NoteRpcService {

    @Resource
    private NoteFeignApi noteFeignApi;

    /**
     * 获取笔记作者 ID
     */
    public Long getNoteCreatorId(Long noteId) {
        try {
            FindNoteDetailReqDTO reqDTO = new FindNoteDetailReqDTO(noteId);
            Response<FindNoteDetailRspDTO> response = noteFeignApi.findNoteDetail(reqDTO);

            if (response.isSuccess() && Objects.nonNull(response.getData())) {
                return response.getData().getCreatorId();
            } else {
                log.warn("==> RPC 调用 Note 模块获取笔记信息失败，noteId: {}, msg: {}", noteId, response.getMessage());
            }
        } catch (Exception e) {
            log.error("==> RPC 调用 Note 模块获取笔记信息异常，noteId: {}", noteId, e);
        }
        return null;
    }
}
