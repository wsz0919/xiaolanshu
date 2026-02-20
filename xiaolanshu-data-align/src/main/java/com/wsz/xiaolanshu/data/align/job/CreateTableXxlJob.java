package com.wsz.xiaolanshu.data.align.job;

import com.wsz.xiaolanshu.data.align.constant.TableConstants;
import com.wsz.xiaolanshu.data.align.mapper.CreateTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-02 14:42
 * @Company:
 */
@Component
@RefreshScope
public class CreateTableXxlJob {

    /**
     * 表总分片数
     */
    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private CreateTableMapper createTableMapper;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("createTableJobHandler")
    public void createTableJobHandler() throws Exception {
        // 表后缀
        String date = LocalDate.now().plusDays(0) // 明日的日期
                .format(DateTimeFormatter.ofPattern("yyyyMMdd")); // 转字符串

        XxlJobHelper.log("## 开始创建日增量数据表，日期: {}...", date);

        if (tableShards > 0) {
            for (int hashKey = 0; hashKey < tableShards; hashKey++) {
                // 表名后缀
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashKey);

                // 创建表
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }

        XxlJobHelper.log("## 结束创建日增量数据表，日期: {}...", date);
    }

}
