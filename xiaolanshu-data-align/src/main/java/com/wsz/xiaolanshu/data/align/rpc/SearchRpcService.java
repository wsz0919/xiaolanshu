package com.wsz.xiaolanshu.data.align.rpc;

import com.wsz.xiaolanshu.search.api.SearchFeignApi;
import com.wsz.xiaolanshu.search.dto.RebuildNoteDocumentReqDTO;
import com.wsz.xiaolanshu.search.dto.RebuildUserDocumentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-06 16:51
 * @Company:
 */
@Component
public class SearchRpcService {

    @Resource
    private SearchFeignApi searchFeignApi;

    /**
     * 调用重建笔记文档接口
     * @param noteId
     */
    public void rebuildNoteDocument(Long noteId) {
        RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO = RebuildNoteDocumentReqDTO.builder()
                .id(noteId)
                .build();

        searchFeignApi.rebuildNoteDocument(rebuildNoteDocumentReqDTO);
    }

    /**
     * 调用重建用户文档接口
     * @param userId
     */
    public void rebuildUserDocument(Long userId) {
        RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO = RebuildUserDocumentReqDTO.builder()
                .id(userId)
                .build();

        searchFeignApi.rebuildUserDocument(rebuildUserDocumentReqDTO);
    }

}
