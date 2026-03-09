package com.wsz.xiaolanshu.search.api;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.search.constant.ApiConstants;
import com.wsz.xiaolanshu.search.dto.RebuildNoteDocumentReqDTO;
import com.wsz.xiaolanshu.search.dto.RebuildUserDocumentReqDTO;
import com.wsz.xiaolanshu.search.dto.SearchNoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date  2026-02-06 16:44
 * @Company:
*/
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface SearchFeignApi {

    String PREFIX = "/search";

    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/note/document/rebuild")
    Response<?> rebuildNoteDocument(@RequestBody RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);


    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/user/document/rebuild")
    Response<?> rebuildUserDocument(@RequestBody RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);

    /**
     * 根据笔记 ID 集合，从 ES 批量获取聚合并格式化后的笔记文档数据
     */
    @PostMapping(value = PREFIX + "/note/document/searchByIds")
    Response<List<SearchNoteDTO>> searchNotesByIds(@RequestBody List<Long> noteIds);
}
