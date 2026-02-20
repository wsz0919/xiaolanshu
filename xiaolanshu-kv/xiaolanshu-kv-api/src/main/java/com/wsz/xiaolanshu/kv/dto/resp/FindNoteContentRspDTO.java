package com.wsz.xiaolanshu.kv.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-09 19:11
 * @Company:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentRspDTO {

    /**
     * 笔记内容 UUID
     */
    private UUID uuid;

    /**
     * 笔记内容
     */
    private String content;

}
