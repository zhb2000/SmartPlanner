package com.my.smartplanner.util;

import android.database.Cursor;

/**
 * 数据库的一些工具
 */
public class DBUtil {
    /**
     * 用列名获取Cursor中的INTEGER数据
     *
     * @param cursor     Cursor对象
     * @param columnName 列名
     * @return int类型
     */
    public static int getIntByName(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * 用列名获取Cursor中作为布尔值的INTEGER数据：
     * 0为假，1为真
     *
     * @param cursor     Cursor对象
     * @param columnName 列名
     * @return boolean类型
     */
    public static Boolean getBooleanByName(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
    }

    /**
     * 用列名获取Cursor中的TEXT数据
     *
     * @param cursor     Cursor对象
     * @param columnName 列名
     * @return String类型数据
     */
    public static String getStringByName(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }
}
