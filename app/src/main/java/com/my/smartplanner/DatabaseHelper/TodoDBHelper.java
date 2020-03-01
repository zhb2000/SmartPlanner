package com.my.smartplanner.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
            + "tag TEXT, "//标签
            + "date TEXT, "//截止日期，格式：yyyy-MM-dd
            + "create_time TEXT NOT NULL, "//创建时间，格式:yyyy-MM-dd HH:mm:ss
            + "edit_time TEXT, "//上次编辑时间，格式：yyyy-MM-dd HH:mm:ss
            + "complete_time TEXT)";//完成时间，格式：yyyy-MM-dd HH:mm:ss
    /**
     * TodoTag表建表语句
     */
    private static final String CREATE_TODO_TAG = "CREATE TABLE TodoTag (tag_name TEXT PRIMARY KEY)";
    /**
     * 最新的数据库版本号
     */
    public static final int LATEST_VERSION = 2;
    /**
     * 数据库文件名
     */
    public static final String DB_NAME = "TodoDatabase.db";
    /**
     * 保存创建对象时的 Context 参数
     */
    private Context mContext;

    /**
     * 创建并获取TodoDatabaseHelper对象
     *
     * @param context 传入当前的Context
     * @return 创建的TodoDatabaseHelper对象
     */
    public static TodoDBHelper getDBHelper(Context context) {
        return new TodoDBHelper(context, DB_NAME, null, LATEST_VERSION);
    }

    /**
     * 构造函数，创建用于操作数据库的helper
     *
     * @param context to use for locating paths to the the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1)
     */
    public TodoDBHelper(Context context, String name,
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
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL(CREATE_TODO_TAG);
        }
    }
}
