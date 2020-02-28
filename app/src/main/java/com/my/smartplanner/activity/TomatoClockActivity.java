package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.my.smartplanner.R;

/**
 * 番茄钟Activity
 */
public class TomatoClockActivity extends AppCompatActivity {

    /**
     * 默认工作时长（分钟）
     */
    public static final int DEFAULT_WORK_LEN = 25;
    /**
     * 默认休息时长（分钟）
     */
    public static final int DEFAULT_REST_LEN = 5;
    /**
     * 默认番茄钟个数
     */
    public static final int DEFAULT_CLOCK_CNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_clock);

        Toolbar toolbar = findViewById(R.id.tomato_clock_toolbar);
        final EditText titleEditText = findViewById(R.id.tomato_clock_title);
        final EditText workLenEditText = findViewById(R.id.tomato_clock_work_len);
        final EditText restLenEditText = findViewById(R.id.tomato_clock_rest_len);
        final EditText countEditText = findViewById(R.id.tomato_clock_count);
        Button startButton = findViewById(R.id.tomato_clock_start_button);

        //Toolbar相关操作
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        workLenEditText.setText(""+DEFAULT_WORK_LEN);
        restLenEditText.setText(""+DEFAULT_REST_LEN);
        countEditText.setText(""+DEFAULT_CLOCK_CNT);

        //button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int workLen = Integer.parseInt(workLenEditText.getText().toString());
                int restLen = Integer.parseInt(restLenEditText.getText().toString());
                int count = Integer.parseInt(countEditText.getText().toString());
                String title;
                if (TextUtils.isEmpty(titleEditText.getText())) {
                    title = "default title";//TODO tomato title
                } else {
                    title = titleEditText.getText().toString();
                }
                //TODO number string null
                Toast.makeText(TomatoClockActivity.this, "work:" + workLen + " rest:" + restLen + " count:" + count, Toast.LENGTH_SHORT).show();
                //TomatoClockOngoingActivity.startTheActivity(TomatoClockActivity.this, workLen, restLen, count, title);
                startActivity(new Intent(TomatoClockActivity.this, TomatoClockOngoingActivity.class));
            }
        });

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
