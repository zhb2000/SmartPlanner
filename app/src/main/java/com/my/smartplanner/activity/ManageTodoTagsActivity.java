package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

import com.my.smartplanner.R;

/**
 * 管理待办标签的Activity
 */
public class ManageTodoTagsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_todo_tags);

        //Toolbar相关操作
        Toolbar toolbar = findViewById(R.id.manage_todo_tags_activity_toolbar);
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
