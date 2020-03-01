package com.my.smartplanner.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;

public class TomatoDBHelper extends SQLiteOpenHelper {
    /**
     * TomatoHistory表的建表语句
     */
    private static final String CREATE_TOMATO_HISTORY = "CREATE TABLE TomatoHistory ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "//id，主键
            + "title TEXT NOT NULL, "//标题
            + "is_successful INTEGER NOT NULL, "//是否成功，0为假，1为真
            + "time_sum INTEGER NOT NULL, "//总时长（分钟）
            + "work_sum INTEGER NOT NULL, "//工作总时长（分钟）
            + "rest_sum INTEGER NOT NULL, "//休息总时长（分钟）
            + "work_len INTEGER NOT NULL, "//单个工作时长（分钟）
            + "rest_len INTEGER NOT NULL, "//单个休息时长（分钟）
            + "clock_cnt INTEGER NOT NULL, "//计划的重复次数
            + "start_time TEXT NOT NULL, "//开始时间，格式：yyyy-MM-dd HH:mm:ss
            + "end_time TEXT NOT NULL)";//结束时间，格式：yyyy-MM-dd HH:mm:ss
    /**
     * 列名
     */
    public static final String
            ID_COL = "id",
            TITLE_COL = "title",
            IS_SUCCESSFUL_COL = "is_successful",
            TIME_SUM_COL = "time_sum",
            WORK_SUM_COL = "work_sum",
            REST_SUM_COL = "rest_sum",
            WORK_LEN_COL = "work_len",
            REST_LEN_COL = "rest_len",
            CLOCK_CNT_COL = "clock_cnt",
            START_TIME_COL = "start_time",
            END_TIME_COL = "end_time";
    /**
     * 最新的数据库版本号
     */
    public static final int LATEST_VERSION = 1;
    /**
     * 数据库文件名
     */
    public static final String DB_NAME = "TomatoDatabase.db";
    /**
     * 保存创建对象时的 Context 参数
     */
    private Context mContext;

    /**
     * 获取可写的数据库
     *
     * @param context 传入当前的Context
     * @return 可写的数据库
     */
    public static SQLiteDatabase getWDB(Context context) {
        return new TomatoDBHelper(context).getWritableDatabase();
    }

    /**
     * 插入一条新记录
     */
    public static void insetRecord(
            Context context,
            String title,
            boolean isSuccessful,
            int timeSum,
            int workSum,
            int restSum,
            int workLen,
            int restLen,
            int clockCnt,
            Calendar startTime,
            Calendar endTime) {
        SQLiteDatabase db = getWDB(context);
        String startTimeStr = CalendarUtil.calendarToString(startTime, "yyyy-MM-dd HH:mm:ss");
        String endTimeStr = CalendarUtil.calendarToString(endTime, "yyyy-MM-dd HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(TITLE_COL, title);
        values.put(IS_SUCCESSFUL_COL, isSuccessful ? 1 : 0);
        values.put(TIME_SUM_COL, timeSum);
        values.put(WORK_SUM_COL, workSum);
        values.put(REST_SUM_COL, restSum);
        values.put(WORK_LEN_COL, workLen);
        values.put(REST_LEN_COL, restLen);
        values.put(CLOCK_CNT_COL, clockCnt);
        values.put(START_TIME_COL, startTimeStr);
        values.put(END_TIME_COL, endTimeStr);
        db.insert("TomatoHistory", null, values);
    }

    /**
     * 自定义的构造方法
     *
     * @param context 传入当前的Context
     */
    public TomatoDBHelper(Context context) {
        super(context, DB_NAME, null, LATEST_VERSION);
        mContext = context;
    }

    /**
     * 构造函数，创建用于操作数据库的helper
     *
     * @param context to use for locating paths to the the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1)
     */
    public TomatoDBHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TOMATO_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
