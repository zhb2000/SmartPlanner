package com.my.smartplanner.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * 定义的SQL数据库类型用于存放每日步数
 */
public class HealthDBHelper extends SQLiteOpenHelper {

    //context：上下文
    //name：数据库名称
    //factory：游标工厂
    //version：数据库版本号（必须大于0）
    public HealthDBHelper(@Nullable Context context) {
        super(context, "UserDB", null, 1);
    }

    //创建表
    @Override
    public void onCreate(SQLiteDatabase arg0) {
        arg0.execSQL("create table UserInfo(id integer primary key autoincrement,date integer,steps integer)");
    }

    //更新数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}


