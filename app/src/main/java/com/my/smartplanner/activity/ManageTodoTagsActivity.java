package com.my.smartplanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.adapter.TodoTagItemAdapter;
import com.my.smartplanner.item.TodoTagListItem;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * 管理待办标签的Activity
 */
public class ManageTodoTagsActivity extends AppCompatActivity {

    private boolean modified = false;
    private List<TodoTagListItem> itemList = new LinkedList<>();
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    public static void startTheActivityForResult(Context packageContext, Activity activity, int requestCode) {
        Intent intent = new Intent(packageContext, ManageTodoTagsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

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
        SQLiteDatabase db = TodoDBHelper.getWDB(this);
        Cursor cursor = db.rawQuery("SELECT DISTINCT tag FROM TodoTag", null);
        if (cursor.moveToFirst()) {
            do {
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                TodoTagListItem item = new TodoTagListItem(tag);
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    /**
     * 设置RecyclerView
     */
    private void recyclerViewSetting() {
        TodoTagItemAdapter todoTagItemAdapter = new TodoTagItemAdapter(itemList, this);
        recyclerView.setAdapter(todoTagItemAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 加载数据异步任务类
     */
    private static class LoadTask extends AsyncTask<Void, Integer, Boolean> {
        private final WeakReference<ManageTodoTagsActivity> weakActivity;

        LoadTask(ManageTodoTagsActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            weakActivity.get().fillList();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                ManageTodoTagsActivity activity = weakActivity.get();
                if (!(activity == null || activity.isFinishing())) {
                    activity.recyclerViewSetting();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_todo_tags);
        findViews();
        toolbarSetting();
        new LoadTask(this).execute();
    }

    /**
     * 菜单选中事件：返回箭头
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//点击返回的箭头
            super.onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(modified ? RESULT_OK : RESULT_CANCELED);
        super.onBackPressed();
    }

    public void setModified() {
        modified = true;
    }
}
