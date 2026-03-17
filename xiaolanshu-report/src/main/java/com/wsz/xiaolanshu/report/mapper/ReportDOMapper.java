package com.wsz.xiaolanshu.report.mapper;

import com.wsz.xiaolanshu.report.domain.dataobject.ReportDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    int insertSelective(ReportDO record);

    int updateByPrimaryKeySelective(ReportDO record);

    ReportDO selectByPrimaryKey(Long id);

    /**
     * 分页查询举报列表
     */
    List<ReportDO> selectPageList(@Param("offset") int offset,
                                  @Param("pageSize") int pageSize,
                                  @Param("status") Integer status);

    /**
     * 查询举报总数
     */
    long selectCount(@Param("status") Integer status);
}
