package com.my.smartplanner.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.my.smartplanner.item.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;

/**
 * 用于操作 TodoDatabase.db 数据库的 helper
 */
public class TodoDBHelper extends SQLiteOpenHelper {

    /**
     * TodoList表建表语句
     */
    private static final String CREATE_TODO_LIST = "CREATE TABLE TodoList ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "//id，主键
            + "title TEXT NOT NULL, "//标题
            + "is_complete INTEGER NOT NULL, "//是否完成，0为假，1为真
            + "is_star INTEGER NOT NULL, "//是否加星，0为假，1为真
            + "alarm TEXT, "//闹钟提醒时间，格式：yyyy-MM-dd HH:mm:ss
            + "note TEXT, "//备注
            + "end_date TEXT, "//截止日期，格式：yyyy-MM-dd
            + "create_time TEXT NOT NULL, "//创建时间，格式:yyyy-MM-dd HH:mm:ss
            + "complete_time TEXT)";//完成时间，格式：yyyy-MM-dd HH:mm:ss
    public static final String ALARM_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String END_DATE_PATTERN = "yyyy-MM-dd";
    public static final String CREATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String COMPLETE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * TodoTag表建表语句
     */
    private static final String CREATE_TODO_TAG =
            "CREATE TABLE TodoTag ("
                    + "id INTEGER NOT NULL, "//id
                    + "tag TEXT NOT NULL)";//标签

    /**
     * 最新的数据库版本号
     */
    private static final int LATEST_VERSION = 1;

    /**
     * 数据库文件名
     */
    private static final String DB_NAME = "TodoDatabase.db";

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
        return new TodoDBHelper(context).getWritableDatabase();
    }

    /**
     * 自定义的构造函数
     *
     * @param context 传入当前的Context
     */
    private TodoDBHelper(Context context) {
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
    private TodoDBHelper(Context context, String name,
                         SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_LIST);
        db.execSQL(CREATE_TODO_TAG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 删除待办
     */
    public static void deleteRecord(Context context, int id) {
        SQLiteDatabase db = getWDB(context);
        db.execSQL("DELETE FROM TodoList WHERE id = ?", new String[]{String.valueOf(id)});
        db.execSQL("DELETE FROM TodoTag WHERE id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 插入待办
     */
    public static void insetRecord(Context context, TodoListItem item) {
        //TodoList表
        ContentValues values = new ContentValues();
        values.put("title", item.getTitle());
        values.put("is_complete", item.getIsComplete() ? 1 : 0);
        values.put("is_star", item.getIsStar() ? 1 : 0);
        values.put("alarm", CalendarUtil.calendarToString(item.getAlarm(), ALARM_PATTERN));
        values.put("note", item.getNote());
        values.put("end_date", CalendarUtil.calendarToString(item.getEndDate(), END_DATE_PATTERN));
        values.put("create_time", CalendarUtil.calendarToString(item.getCreateTime(), CREATE_TIME_PATTERN));
        values.put("complete_time", CalendarUtil.calendarToString(item.getCompleteTime(), COMPLETE_TIME_PATTERN));
        SQLiteDatabase db = getWDB(context);
        db.insert("TodoList", null, values);
        //给对象的id赋值
        Cursor cursor = db.rawQuery("SELECT MAX(id) AS item_id FROM TodoList", null);
        cursor.moveToFirst();
        item.setId(cursor.getInt(cursor.getColumnIndex("item_id")));
        cursor.close();
        //TodoTag表
        for (String tag : item.getTags()) {
            db.execSQL("INSERT OR IGNORE INTO TodoTag (id, tag) values(?, ?)",
                    new String[]{String.valueOf(item.getId()), tag});
        }
        db.close();
    }

    /**
     * 修改待办
     */
    public static void updateRecord(Context context, TodoListItem item) {
        //TodoList表
        ContentValues values = new ContentValues();
        values.put("title", item.getTitle());
        values.put("is_complete", item.getIsComplete() ? 1 : 0);
        values.put("is_star", item.getIsStar() ? 1 : 0);
        values.put("alarm", CalendarUtil.calendarToString(item.getAlarm(), ALARM_PATTERN));
        values.put("note", item.getNote());
        values.put("end_date", CalendarUtil.calendarToString(item.getEndDate(), END_DATE_PATTERN));
        values.put("create_time", CalendarUtil.calendarToString(item.getCreateTime(), CREATE_TIME_PATTERN));
        values.put("complete_time", CalendarUtil.calendarToString(item.getCompleteTime(), COMPLETE_TIME_PATTERN));
        SQLiteDatabase db = getWDB(context);
        db.update("TodoList", values, "id = ?", new String[]{String.valueOf(item.getId())});
        //TodoTag表
        db.execSQL("DELETE FROM TodoTag WHERE id = ?", new String[]{String.valueOf(item.getId())});
        for (String tag : item.getTags()) {
            db.execSQL("INSERT OR IGNORE INTO TodoTag (id, tag) values(?, ?)",
                    new String[]{String.valueOf(item.getId()), tag});
        }
        db.close();
    }

    /**
     * 更新数据库中星标状态，即is_star列
     */
    public static void updateStar(Context context, int id, boolean isStar) {
        SQLiteDatabase db = getWDB(context);
        ContentValues values = new ContentValues();
        int isStarInt = isStar ? 1 : 0;
        values.put("is_star", isStarInt);
        db.update("TodoList", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 更新数据库中完成状态，即is_complete列和complete_time列
     */
    public static void updateComplete(Context context, int id, boolean isComplete, Calendar completeTime) {
        SQLiteDatabase db = getWDB(context);
        String completeTimeStr = CalendarUtil.calendarToString(completeTime, COMPLETE_TIME_PATTERN);
        ContentValues values = new ContentValues();
        values.put("is_complete", isComplete);
        values.put("complete_time", completeTimeStr);
        db.update("TodoList", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 查询新标签名字是否已经存在
     */
    public static boolean hasTag(Context context, String tag) {
        SQLiteDatabase db = getWDB(context);
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) AS tag_count FROM TodoTag WHERE tag = ?",
                new String[]{tag});
        cursor.moveToFirst();
        int tagCount = cursor.getInt(
                cursor.getColumnIndex("tag_count"));
        cursor.close();
        db.close();
        return tagCount > 0;
    }

    /**
     * 重命名标签
     */
    public static void renameTag(Context context, String oldTag, String newTag) {
        SQLiteDatabase db = getWDB(context);
        db.execSQL("UPDATE TodoTag " +
                        "SET tag = ? " +
                        "WHERE tag = ?",
                new String[]{newTag, oldTag});
        db.close();
    }

    /**
     * 删除标签
     */
    public static void deleteTag(Context context, String tag) {
        SQLiteDatabase db = getWDB(context);
        db.execSQL("DELETE FROM TodoTag WHERE tag = ?", new String[]{tag});
        db.close();
    }
}
