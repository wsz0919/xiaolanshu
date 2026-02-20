package com.wsz.xiaolanshu.note.api;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.constant.ApiConstants;
import com.wsz.xiaolanshu.note.dto.req.FindNoteDetailReqDTO;
import com.wsz.xiaolanshu.note.dto.resp.FindNoteDetailRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-20 12:17
 * @Company:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface NoteFeignApi {

    String PREFIX = "/note";

    /**
     * 获取笔记基础信息 (供内部 RPC 调用)
     */
    @PostMapping(value = PREFIX + "/detail")
    Response<FindNoteDetailRspDTO> findNoteDetail(@Validated @RequestBody FindNoteDetailReqDTO findNoteDetailReqDTO);
}
