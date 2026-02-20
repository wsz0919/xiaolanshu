package com.wsz.xiaolanshu.biz.repository;

import com.wsz.xiaolanshu.biz.domain.dataobject.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-09 18:47
 * @Company:
 */
public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {

}
