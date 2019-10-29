package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import android.os.Bundle;
import android.view.MenuItem;

import com.my.smartplanner.R;

/**
 * 步数统计的Activity
 */
public class StepStatisticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_statistic);

        //Toolbar相关操作
        Toolbar toolbar = findViewById(R.id.step_statistic_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
