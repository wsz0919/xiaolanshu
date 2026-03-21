package com.wsz.xiaolanshu.report.rpc;

import com.wsz.xiaolanshu.note.api.NoteFeignApi;
import com.wsz.xiaolanshu.note.dto.req.AdminUpdateNoteStatusReqVO;
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
public class NoteRpcService {

    @Resource
    private NoteFeignApi noteFeignApi;

    public void offlineNote(Long targetId, Integer status) {
        AdminUpdateNoteStatusReqVO vo = new AdminUpdateNoteStatusReqVO();
        vo.setId(targetId);
        vo.setStatus(status);

        noteFeignApi.updateNoteStatus(vo);
    }
}
