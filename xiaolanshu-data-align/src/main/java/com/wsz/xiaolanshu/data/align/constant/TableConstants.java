package com.wsz.xiaolanshu.data.align.constant;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-02 14:47
 * @Company:
 */
public class TableConstants {

    /**
     * 表名中的分隔符
     */
    private static final String TABLE_NAME_SEPARATE = "_";

    /**
     * 拼接表名后缀
     * @param hashKey
     * @return
     */
    public static String buildTableNameSuffix(String date, long hashKey) {
        // 拼接完整的表名
        return date + TABLE_NAME_SEPARATE + hashKey;
    }

}
