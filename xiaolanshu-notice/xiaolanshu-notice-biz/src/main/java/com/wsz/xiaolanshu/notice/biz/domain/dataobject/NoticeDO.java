package com.wsz.xiaolanshu.notice.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDO {
    private Long id;
    private Long receiverId;
    private Long senderId;
    private Integer type;
    private Integer subType;
    private Long targetId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isDeleted;
}