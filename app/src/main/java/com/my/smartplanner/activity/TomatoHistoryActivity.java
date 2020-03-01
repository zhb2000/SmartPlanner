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

import com.my.smartplanner.DatabaseHelper.TomatoDBHelper;
import com.my.smartplanner.adapter.TomatoHistoryItemAdapter;
import com.my.smartplanner.item.TomatoHistoryListItem;
import com.my.smartplanner.R;
import com.my.smartplanner.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

public class TomatoHistoryActivity extends AppCompatActivity {

    private List<TomatoHistoryListItem> listItems = new ArrayList<>();

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    /**
     * 获取控件实例
     */
    private void findViews() {
        toolbar = findViewById(R.id.tomato_history_toolbar);
        recyclerView = findViewById(R.id.tomato_history_recycler_view);
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
        SQLiteDatabase db = TomatoDBHelper.getWDB(this);
        Cursor cursor = db.rawQuery("SELECT * FROM TomatoHistory", null);
        if (cursor.moveToFirst()) {
            do {
                String title = DBUtil.getStringByName(cursor, TomatoDBHelper.TITLE_COL);
                boolean isSuccessful = DBUtil.getBooleanByName(cursor, TomatoDBHelper.IS_SUCCESSFUL_COL);
                int timeSum = DBUtil.getIntByName(cursor, TomatoDBHelper.TIME_SUM_COL);
                int workSum = DBUtil.getIntByName(cursor, TomatoDBHelper.WORK_SUM_COL);
                int restSum = DBUtil.getIntByName(cursor, TomatoDBHelper.REST_SUM_COL);
                int workLen = DBUtil.getIntByName(cursor, TomatoDBHelper.WORK_LEN_COL);
                int restLen = DBUtil.getIntByName(cursor, TomatoDBHelper.REST_LEN_COL);
                int clockCnt = DBUtil.getIntByName(cursor, TomatoDBHelper.CLOCK_CNT_COL);
                String startTimeStr = DBUtil.getStringByName(cursor, TomatoDBHelper.START_TIME_COL);
                String endTimeStr = DBUtil.getStringByName(cursor, TomatoDBHelper.END_TIME_COL);
                TomatoHistoryListItem item = new TomatoHistoryListItem(
                        title, isSuccessful, timeSum, workSum, restSum,
                        workLen, restLen, clockCnt, startTimeStr, endTimeStr);
                listItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 设置RecyclerView
     */
    private void recyclerViewSetting() {
        TomatoHistoryItemAdapter adapter = new TomatoHistoryItemAdapter(listItems);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_history);
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
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return true;
    }
}
