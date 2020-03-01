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
import android.widget.TextView;

import com.my.smartplanner.R;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

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

    private int workLen, restLen, clockCnt;

    private IndicatorSeekBar workLenSeekBar, restLenSeekBar, clockCntSeekBar;
    private TextView workLenTextView, restLenTextView, clockCntTextView;
    private Button startButton, defaultButton;
    private Toolbar toolbar;
    private EditText titleEditText;

    /**
     * 获取控件实例
     */
    private void findViews() {
        toolbar = findViewById(R.id.tomato_clock_toolbar);
        workLenSeekBar = findViewById(R.id.tomato_clock_work_seek_bar);
        restLenSeekBar = findViewById(R.id.tomato_clock_rest_seek_bar);
        clockCntSeekBar = findViewById(R.id.tomato_clock_count_seek_bar);
        workLenTextView = findViewById(R.id.tomato_clock_work_text);
        restLenTextView = findViewById(R.id.tomato_clock_rest_text);
        clockCntTextView = findViewById(R.id.tomato_clock_count_text);
        startButton = findViewById(R.id.tomato_clock_start_button);
        defaultButton = findViewById(R.id.tomato_clock_default_button);
        titleEditText = findViewById(R.id.tomato_clock_title);
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
     * 设置开始按钮
     */
    private void startButtonSetting() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title;
                if (TextUtils.isEmpty(titleEditText.getText())) {
                    title = getString(R.string.tomato_default_title);
                } else {
                    title = titleEditText.getText().toString();
                }
                //Toast.makeText(TomatoClockActivity.this, "work:" + workLen + " rest:" + restLen + " count:" + count, Toast.LENGTH_SHORT).show();
                TomatoOngoingActivity.startTheActivity(
                        TomatoClockActivity.this, workLen, restLen, clockCnt, title);
            }
        });
    }

    /**
     * 设置默认按钮
     */
    private void defaultButtonSetting() {
        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNumber(DEFAULT_WORK_LEN, DEFAULT_REST_LEN, DEFAULT_CLOCK_CNT);
            }
        });
    }

    /**
     * 将SeekBar、TextView、成员变量绑定
     *
     * @param seekBar  SeekBar控件对象
     * @param textView TextView控件对象
     * @param type     与哪个成员变量绑定
     *                 <li>1 - workLen</li>
     *                 <li>2 - restLen</li>
     *                 <li>3 - clockCnt</li>
     */
    private void bindSeekBar(IndicatorSeekBar seekBar,
                             final TextView textView, final int type) {
        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                int progress = seekParams.progress;
                textView.setText(String.valueOf(progress));
                if (type == 1) {
                    workLen = progress;
                } else if (type == 2) {
                    restLen = progress;
                } else {
                    clockCnt = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
    }

    /**
     * 给数字设置相应的值
     *
     * @param workLen  单次工作时长
     * @param restLen  单次休息时长
     * @param clockCnt 番茄钟个数
     */
    private void setNumber(int workLen, int restLen, int clockCnt) {
        this.workLen = workLen;
        this.restLen = restLen;
        this.clockCnt = clockCnt;
        workLenSeekBar.setProgress(workLen);
        restLenSeekBar.setProgress(restLen);
        clockCntSeekBar.setProgress(clockCnt);
        workLenTextView.setText(String.valueOf(workLen));
        restLenTextView.setText(String.valueOf(restLen));
        clockCntTextView.setText(String.valueOf(clockCnt));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_clock);
        findViews();
        toolbarSetting();
        startButtonSetting();
        defaultButtonSetting();
        bindSeekBar(workLenSeekBar, workLenTextView, 1);
        bindSeekBar(restLenSeekBar, restLenTextView, 2);
        bindSeekBar(clockCntSeekBar, clockCntTextView, 3);
        setNumber(DEFAULT_WORK_LEN, DEFAULT_REST_LEN, DEFAULT_CLOCK_CNT);
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
                Intent intent = new Intent(this, TomatoHistoryActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
