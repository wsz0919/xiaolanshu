package com.wsz.xiaolanshu.report.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDO {
    private Long id;
    private Long reporterId; // 举报人ID
    private Integer targetType; // 1:用户 2:笔记 3:评论
    private Long targetId; // 被举报的对象ID
    private String reportReason; // 举报原因
    private String reportDetail; // 补充说明
    private String images; // 举报截图(逗号分隔)
    private Integer status; // 0:待处理 1:已处理-违规 2:已处理-不违规/驳回
    private String processRemark; // 后台处理备注
    private Long processorId; // 处理人管理员ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
