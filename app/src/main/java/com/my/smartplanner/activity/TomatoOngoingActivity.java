package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.jaeger.library.StatusBarUtil;
import com.my.smartplanner.DatabaseHelper.TomatoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;

import cn.iwgang.countdownview.CountdownView;

/**
 * 番茄钟计时
 */
public class TomatoOngoingActivity extends AppCompatActivity {

    private static final String WORK_LEN_EXTRA_NAME = "workLen";
    private static final String REST_LEN_EXTRA_NAME = "restLen";
    private static final String CLOCK_CNT_EXTRA_NAME = "clockCnt";
    private static final String TITLE_EXTRA_NAME = "title";

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
     * 已完成的工作时段数目
     */
    private int workCnt;
    /**
     * 已完成的休息时间段数目
     */
    private int restCnt;
    /**
     * 当前是否为工作时间段
     */
    private boolean isAtWork;
    /**
     * 工作总时长（分钟）
     */
    private int workTimeSum;
    /**
     * 休息总时长（分钟）
     */
    private int restTimeSum;
    /**
     * 开始时间
     */
    private Calendar startTime;
    /**
     * 结束时间
     */
    private Calendar endTime;

    /**
     * 倒计时组件
     */
    private CountdownView countdownView;
    private TextView titleTextView;
    private MaterialButton skipRestButton;
    private Toolbar toolbar;

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
        Intent intent = new Intent(packageContext, TomatoOngoingActivity.class);
        intent.putExtra(WORK_LEN_EXTRA_NAME, workLen);
        intent.putExtra(REST_LEN_EXTRA_NAME, restLen);
        intent.putExtra(CLOCK_CNT_EXTRA_NAME, count);
        intent.putExtra(TITLE_EXTRA_NAME, title);
        packageContext.startActivity(intent);
    }

    /**
     * 获取已经走了多少分钟
     *
     * @param countdown 倒计时控件
     * @param setMinute 设置的倒计时分钟数
     * @return 已经走的分钟数
     */
    private static int hasGoneMinute(CountdownView countdown, int setMinute) {
        long setMilli = setMinute * 60 * 1000;
        long remainMilli = countdown.getRemainTime();
        long goneMinute = (setMilli - remainMilli) / 1000 / 60;
        return (int) goneMinute;
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

    /**
     * <p>番茄钟开始</p>
     * <p>将工作次数、休息次数、工作时长、休息时长置零</p>
     * <p>切换到工作模式，开始工作计时</p>
     */
    private void startTomato() {
        startTime = Calendar.getInstance();
        workCnt = 0;
        restCnt = 0;
        workTimeSum = 0;
        restTimeSum = 0;
        isAtWork = true;
        countdownView.start(CalendarUtil.minuteToMillisecond(workLen));
        toolbar.setTitle(R.string.tomato_working);
        skipRestButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 计时器到时的回调函数，切换工作/休息，或成功结束
     */
    private void switchStatus() {
        if (isAtWork) {//之前工作，现在切换到休息
            workCnt++;//已完成工作 + 1
            workTimeSum += workLen;//工作时长增加一整段
            if (workCnt == clockCount) {//已经成功完成
                tomatoSuccessful();
            } else {//尚未完成
                isAtWork = false;//切换到休息模式
                //休息计时开始
                countdownView.start(CalendarUtil.minuteToMillisecond(restLen));
                toolbar.setTitle(R.string.tomato_resting);
                skipRestButton.setVisibility(View.VISIBLE);
            }
        } else {//之前休息，现在切换到工作
            restCnt++;//已完成休息 + 1
            restTimeSum += restLen;//休息时长增加一整段
            isAtWork = true;//切换到工作模式
            //工作计时开始
            countdownView.start(CalendarUtil.minuteToMillisecond(workLen));
            toolbar.setTitle(R.string.tomato_working);
            skipRestButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 跳过休息时调用
     */
    private void skipRest() {
        if (!isAtWork) {
            restCnt++;//已完成休息 + 1
            restTimeSum += hasGoneMinute(countdownView, restLen);//休息时长增加一部分
            isAtWork = true;//切换到工作模式
            // 工作计时开始
            countdownView.start(CalendarUtil.minuteToMillisecond(workLen));
            toolbar.setTitle(R.string.tomato_working);
            skipRestButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 成功结束时调用
     */
    private void tomatoSuccessful() {
        int allTimeSum = workTimeSum + restTimeSum;
        endTime = Calendar.getInstance();
        saveData(true);
        super.onBackPressed();
    }

    /**
     * <p>失败结束时调用</p>
     * <p>在按下返回键时调用</p>
     */
    private void tomatoFail() {
        endTime = Calendar.getInstance();
        //修改工作总时长/休息总时长
        if (isAtWork) {
            workTimeSum += hasGoneMinute(countdownView, workLen);
        } else {
            restTimeSum += hasGoneMinute(countdownView, restLen);
        }
        if (workTimeSum < 1) {//工作时长小于1分钟，时长过短的失败
            tomatoTooShort();
        } else {
            int allTimeSum = workTimeSum + restTimeSum;
            Toast.makeText(this, "失败，总时长：" + allTimeSum + "分钟", Toast.LENGTH_LONG).show();
            saveData(false);
            super.onBackPressed();
        }
    }

    /**
     * <p>失败但时长过短时调用</p>
     * <p>在tomatoFail()中调用</p>
     */
    private void tomatoTooShort() {
        Toast.makeText(this, getString(R.string.tomato_too_short_msg), Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    /**
     * 保存数据到数据库中
     *
     * @param isSuccessful 是否成功
     */
    private void saveData(boolean isSuccessful) {
        TomatoDBHelper.insetRecord(this,
                title,
                isSuccessful,
                workTimeSum + restTimeSum,
                workTimeSum, restTimeSum,
                workLen,
                restLen,
                clockCount,
                startTime,
                endTime);
    }

    /**
     * 设置Toolbar
     */
    private void toolbarSetting() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    /**
     * 获取控件实例
     */
    private void findViews() {
        titleTextView = findViewById(R.id.tomato_clock_ongoing_title);
        countdownView = findViewById(R.id.tomato_clock_ongoing_countdown);
        skipRestButton = findViewById(R.id.tomato_ongoing_skip_rest);
        toolbar = findViewById(R.id.tomato_ongoing_toolbar);
    }

    private void statusBarAndNavigationBarSetting(){
        StatusBarUtil.setColor(this, getResources()
                .getColor(R.color.tomato_ongoing_dark_bg), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources()
                    .getColor(R.color.tomato_ongoing_dark_bg));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarAndNavigationBarSetting();
        setContentView(R.layout.activity_tomato_ongoing);
        getTheExtra();
        findViews();
        toolbarSetting();

        //title text view
        titleTextView.setText(title);
        //countdown test
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                switchStatus();
            }
        });
        //skip rest toolbar
        skipRestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TomatoOngoingActivity.this.skipRest();
            }
        });

        startTomato();
    }

    /**
     * 按下返回键
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.confirm_to_give_up);//设置对话框标题
        dialogBuilder.setMessage(R.string.give_up_tomato_msg);//设置对话框消息
        //设置对话框“放弃”按钮
        dialogBuilder.setPositiveButton(R.string.give_up, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tomatoFail();
            }
        });
        //设置对话框“取消”按钮
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.show();
    }

    /**
     * 菜单选中事件
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
        }
        return true;
    }
}
