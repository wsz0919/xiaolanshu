package com.wsz.xiaolanshu.biz.repository;

import com.wsz.xiaolanshu.biz.domain.dataobject.CommentContentDO;
import com.wsz.xiaolanshu.biz.domain.dataobject.CommentContentPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-09 14:31
 * @Company:
 */
public interface CommentContentRepository extends CassandraRepository<CommentContentDO, CommentContentPrimaryKey> {

    /**
     * 批量查询评论内容
     * @param noteId
     * @param yearMonths
     * @param contentIds
     * @return
     */
    List<CommentContentDO> findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(
            Long noteId, List<String> yearMonths, List<UUID> contentIds
    );

    /**
     * 删除评论正文
     * @param noteId
     * @param yearMonth
     * @param contentId
     */
    void deleteByPrimaryKeyNoteIdAndPrimaryKeyYearMonthAndPrimaryKeyContentId(Long noteId, String yearMonth, UUID contentId);

    /**
     * 根据完整的主键获取评论内容
     */
    @Query("SELECT * FROM comment_content WHERE note_id = ?0 AND year_month = ?1 AND content_id = ?2")
    CommentContentDO findContentByPrimaryKey(Long noteId, String yearMonth, UUID contentId);
}
