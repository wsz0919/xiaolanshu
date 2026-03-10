package com.wsz.xiaolanshu.note.biz.rpc;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.search.api.SearchFeignApi;
import com.wsz.xiaolanshu.search.dto.SearchNoteDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-10 21:48
 * @Company:
 */
@Component
public class SearchRpcService {

    @Resource
    private SearchFeignApi searchFeignApi;

    /**
     * 根据笔记 ID 集合，从 ES 批量获取聚合并格式化后的笔记文档数据
     * @param noteIds
     * @return
     */
    public List<SearchNoteDTO> searchNotesByIds(List<Long> noteIds) {
        Response<List<SearchNoteDTO>> response = searchFeignApi.searchNotesByIds(noteIds);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }
}
