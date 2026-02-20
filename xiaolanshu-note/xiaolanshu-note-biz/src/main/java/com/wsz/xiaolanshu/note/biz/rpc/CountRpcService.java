package com.wsz.xiaolanshu.note.biz.rpc;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.count.api.CountFeignApi;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdReqDTO;
import com.wsz.xiaolanshu.count.dto.FindNoteCountByIdRspDTO;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-13 16:47
 * @Company:
 */
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 查询笔记计数信息
     * @param noteId
     * @return
     */
    public FindNoteCountByIdRspDTO findNoteCountById(Long noteId) {
        FindNoteCountByIdReqDTO findNoteCountByIdReqDTO = new FindNoteCountByIdReqDTO();
        findNoteCountByIdReqDTO.setNoteId(noteId);

        Response<FindNoteCountByIdRspDTO> response = countFeignApi.findNoteCount(findNoteCountByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

}
