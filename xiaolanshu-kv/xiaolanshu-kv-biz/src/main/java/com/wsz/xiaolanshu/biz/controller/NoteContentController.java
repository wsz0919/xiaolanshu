package com.wsz.xiaolanshu.biz.controller;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.biz.service.NoteContentService;
import com.wsz.xiaolanshu.kv.dto.req.AddNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.BatchFindNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.req.FindNoteContentReqDTO;
import com.wsz.xiaolanshu.kv.dto.resp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-09 19:03
 * @Company:
 */
@RestController
@RequestMapping("/kv")
@Slf4j
public class NoteContentController {

    @Resource
    private NoteContentService noteContentService;

    @PostMapping(value = "/note/content/add")
    public Response<?> addNoteContent(@Validated @RequestBody AddNoteContentReqDTO addNoteContentReqDTO) {
        return noteContentService.addNoteContent(addNoteContentReqDTO);
    }

    @PostMapping(value = "/note/content/find")
    public Response<FindNoteContentRspDTO> findNoteContent(@Validated @RequestBody FindNoteContentReqDTO findNoteContentReqDTO) {
        return noteContentService.findNoteContent(findNoteContentReqDTO);
    }

    @PostMapping(value = "/note/content/delete")
    public Response<?> deleteNoteContent(@Validated @RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }

    @PostMapping(value = "/note/content/batchFind")
    public Response<List<FindNoteContentRspDTO>> findNoteContentBatch(@Validated @RequestBody BatchFindNoteContentReqDTO reqDTO) {
        return noteContentService.findNoteContentBatch(reqDTO);
    }
}
