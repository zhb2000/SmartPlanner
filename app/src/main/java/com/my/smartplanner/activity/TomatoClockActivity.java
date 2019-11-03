package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.my.smartplanner.R;

/**
 * 番茄钟Activity
 */
public class TomatoClockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_clock);

        //Toolbar相关操作
        Toolbar toolbar = findViewById(R.id.tomato_clock_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 加载菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tomato_clock_menu, menu);
        return true;
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
            case R.id.tomato_clock_menu_statistic://点击“番茄钟数据”菜单项
                Intent intent = new Intent(this, TomatoClockStatisticActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
