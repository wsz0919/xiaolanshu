package com.wsz.xiaolanshu.report.mapper;

import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import org.apache.ibatis.annotations.Param;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-03-16 10:42
 * @Company:
 */
public interface ReportDOMapper {

    // 插入举报记录
    int insert(ReportDO reportDO);

    // 更新处理状态
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("processorId") Long processorId,
                     @Param("processRemark") String processRemark);
}
