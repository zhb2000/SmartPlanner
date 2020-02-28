package com.my.smartplanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.my.smartplanner.R;

import cn.iwgang.countdownview.CountdownView;

/**
 * 番茄钟计时
 */
public class TomatoClockOngoingActivity extends AppCompatActivity {

    /**
     * 工作时间长度（分钟）
     */
    private int workLen;
    /**
     * 休息时间长度（分钟）
     */
    private int restLen;
    /**
     * 番茄钟个数
     */
    private int clockCount;
    /**
     * 番茄钟标题
     */
    private String title;

    /**
     * 当前第几个工作时间段
     */
    private int workCnt;
    /**
     * 当前第几个休息时间段
     */
    private int restCnt;
    /**
     * 当前是否为工作时间段
     */
    private boolean isWork;

    private static final String WORK_LEN_EXTRA_NAME = "workLen";
    private static final String REST_LEN_EXTRA_NAME = "restLen";
    private static final String CLOCK_CNT_EXTRA_NAME = "clockCnt";
    private static final String TITLE_EXTRA_NAME = "title";

    /**
     * 启动番茄钟计时Activity
     *
     * @param packageContext 启动方Activity的this
     * @param workLen        工作时间长度（分钟）
     * @param restLen        休息时间长度（分钟）
     * @param count          番茄钟个数
     * @param title          番茄钟标题
     */
    public static void startTheActivity(Context packageContext, int workLen, int restLen, int count, String title) {
        Intent intent = new Intent(packageContext, TomatoClockOngoingActivity.class);
        intent.putExtra(WORK_LEN_EXTRA_NAME, workLen);
        intent.putExtra(REST_LEN_EXTRA_NAME, restLen);
        intent.putExtra(CLOCK_CNT_EXTRA_NAME, count);
        intent.putExtra(TITLE_EXTRA_NAME, title);
        packageContext.startActivity(intent);
    }

    /**
     * 从Intent中取出工作时长、休息时长、番茄钟个数、番茄钟标题
     */
    private void getTheExtra() {
        Intent intent = getIntent();
        workLen = intent.getIntExtra(WORK_LEN_EXTRA_NAME, TomatoClockActivity.DEFAULT_WORK_LEN);
        restLen = intent.getIntExtra(REST_LEN_EXTRA_NAME, TomatoClockActivity.DEFAULT_REST_LEN);
        clockCount = intent.getIntExtra(CLOCK_CNT_EXTRA_NAME, TomatoClockActivity.DEFAULT_CLOCK_CNT);
        title = intent.getStringExtra(TITLE_EXTRA_NAME);
    }

    private void startTomato() {
        workCnt = 0;
        restCnt = 0;
        isWork = true;
    }

    private void switchStatus() {
        if (workCnt == clockCount && restCnt == clockCount - 1) {

        }
        if (isWork) {
            //之前工作，现在切换到休息
        } else {
            //之前休息，现在切换到工作
        }
    }

    private void tomatoSuccessful() {

    }

    private void tomatoFail(){

    }

    private void tomatoTooShort(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_clock_ongoing);

        getTheExtra();

        //title textview
        TextView titleTextView = findViewById(R.id.tomato_clock_ongoing_title);
        titleTextView.setText(title);

        //countdown test
        CountdownView countdownView = findViewById(R.id.tomato_clock_ongoing_countdown);
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                //TODO
            }
        });
        countdownView.start(5000);
    }
}
