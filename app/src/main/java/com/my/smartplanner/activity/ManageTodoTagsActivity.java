package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.item.TodoTagListItem;
import com.my.smartplanner.adapter.TodoTagItemAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * 管理待办标签的Activity
 */
public class ManageTodoTagsActivity extends AppCompatActivity {

    private List<TodoTagListItem> itemList = new LinkedList<>();
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    /**
     * 获取控件实例
     */
    private void findViews() {
        recyclerView = findViewById(R.id.manage_todo_tags_activity_recycler_view);
        toolbar = findViewById(R.id.manage_todo_tags_activity_toolbar);
    }

    /**
     * 设置Toolbar
     */
    private void toolbarSetting() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 装填List
     */
    private void fillList() {
        TodoDBHelper dbHelper = TodoDBHelper.getDBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TodoTag", null);
        if (cursor.moveToFirst()) {
            do {
                String tagName = cursor.getString(cursor.getColumnIndex("tag_name"));
                TodoTagListItem item = new TodoTagListItem(tagName);
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 设置RecyclerView
     */
    private void recyclerViewSetting() {
        TodoTagItemAdapter todoTagItemAdapter = new TodoTagItemAdapter(itemList);
        recyclerView.setAdapter(todoTagItemAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_todo_tags);
        findViews();
        toolbarSetting();
        fillList();
        recyclerViewSetting();
    }

    /**
     * 菜单选中事件
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//点击返回的箭头
            super.onBackPressed();
        }
        return true;
    }
}
