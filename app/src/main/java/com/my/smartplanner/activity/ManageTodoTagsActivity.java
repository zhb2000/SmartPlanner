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

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.TodoTagListItem;
import com.my.smartplanner.adapter.TodoTagItemAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * 管理待办标签的Activity
 */
public class ManageTodoTagsActivity extends AppCompatActivity {

    private List<TodoTagListItem> itemList = new LinkedList<>();
    private TodoTagItemAdapter todoTagItemAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_todo_tags);

        recyclerView=findViewById(R.id.manage_todo_tags_activity_recycler_view);

        //Toolbar相关操作
        Toolbar toolbar = findViewById(R.id.manage_todo_tags_activity_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(this, "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
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

        todoTagItemAdapter=new TodoTagItemAdapter(itemList);
        recyclerView.setAdapter(todoTagItemAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    /**
     * 菜单选中事件
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://点击返回的箭头
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
