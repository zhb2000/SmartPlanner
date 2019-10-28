package com.my.smartplanner.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_TODO_LIST = "CREATE TABLE TodoList ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "title TEXT NOT NULL, "
            + "is_complete INTEGER NOT NULL, "
            + "is_star INTEGER NOT NULL, "
            + "alarm TEXT, "
            + "note TEXT, "
            + "tag TEXT, "
            + "date TEXT, "
            + "create_time TEXT NOT NULL, "
            + "edit_time TEXT, "
            + "complete_time TEXT)";

    private static final String CREATE_TODO_TAG = "CREATE TABLE TodoTag (tag_name TEXT PRIMARY KEY)";

    public static final int NOW_VERSION = 2;

    private Context mContext;

    public TodoDatabaseHelper(Context context, String name,
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
        if(oldVersion==1&&newVersion==2){
            db.execSQL(CREATE_TODO_TAG);
        }
    }
}
