package com.wsz.xiaolanshu.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-07 15:20
 * @Company:
 */
@Table("comment_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentContentDO {

    @PrimaryKey
    private CommentContentPrimaryKey primaryKey;

    private String content;
}
