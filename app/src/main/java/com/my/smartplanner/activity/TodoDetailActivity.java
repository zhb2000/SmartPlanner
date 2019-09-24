package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.SublimePickerFragment;
import com.my.smartplanner.util.DateUtil;
import com.my.smartplanner.util.LogUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * 待办条目详情页面的Activity
 */
public class TodoDetailActivity extends AppCompatActivity {

    public static final int CREATE_MODE = 0;//启动方式为创建模式
    public static final int EDIT_MODE = 1;//启动方式为编辑模式

    private SQLiteDatabase db;
    private int mode;//模式
    private int positionInAdapter;//下标位置
    private int id = 0;//待办条目在数据库中的id
    private String title = null;//标题
    private boolean isComplete = false;//是否完成
    private boolean isStar = false;//是否加星
    private String note = null;//备注
    private String tag = null;//标签
    private String completeTime = null;//完成时间
    private String editTime = null;//修改时间
    private String createTime = null;//创建时间
    private String dateString = null;
    private String alarmString = null;

    private Toolbar toolbar;
    private CheckBox completeCheckbox;//完成复选框
    private CheckBox starCheckbox;//星标复选框
    private EditText titleEditText;//标题文本框
    private EditText tagEditText;//标签文本框
    private EditText noteEditText;//备注文本框
    private LinearLayout selectDateArea;
    private ImageView calendarIcon;
    private TextView selectDateTextView;
    private ImageView deleteDate;
    private LinearLayout selectAlarmArea;
    private ImageView alarmIcon;
    private TextView selectAlarmTextView;
    private ImageView deleteAlarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        //打开数据库
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(TodoDetailActivity.this, "TodoDatabase.db", null, 1);
        db = dbHelper.getWritableDatabase();
        //从intent中提取数据
        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 0);
        if (mode == EDIT_MODE) {
            positionInAdapter = intent.getIntExtra("position_in_adapter", 0);
            id = intent.getIntExtra("id_in_database", 1);
            //利用id在数据库中找到这一行
            Cursor cursor = db.rawQuery("SELECT * FROM TodoList WHERE id = ?", new String[]{"" + id});
            //用这一行的各个字段给对应的成员变量赋值
            cursor.moveToFirst();
            title = cursor.getString(cursor.getColumnIndex("title"));
            isComplete = cursor.getInt(cursor.getColumnIndex("is_complete")) == 1;
            isStar = cursor.getInt(cursor.getColumnIndex("is_star")) == 1;
            note = cursor.getString(cursor.getColumnIndex("note"));
            tag = cursor.getString(cursor.getColumnIndex("tag"));
            completeTime = cursor.getString(cursor.getColumnIndex("complete_time"));
            editTime = cursor.getString(cursor.getColumnIndex("edit_time"));
            createTime = cursor.getString(cursor.getColumnIndex("create_time"));
            dateString = cursor.getString(cursor.getColumnIndex("date"));
            alarmString = cursor.getString(cursor.getColumnIndex("alarm"));
            cursor.close();
        }

        toolbar = findViewById(R.id.todo_detail_toolbar);
        titleEditText = findViewById(R.id.todo_detail_title_edit_text);
        tagEditText = findViewById(R.id.todo_detail_tag_edit_text);
        noteEditText = findViewById(R.id.todo_detail_note_edit_text);
        completeCheckbox = findViewById(R.id.todo_detail_complete);
        starCheckbox = findViewById(R.id.todo_detail_star);
        selectDateArea=findViewById(R.id.todo_detail_select_date_area);
        calendarIcon=findViewById(R.id.todo_detail_calendar_icon);
        selectDateTextView=findViewById(R.id.todo_detail_select_date_text);
        deleteDate=findViewById(R.id.todo_detail_select_date_delete);
        selectAlarmArea=findViewById(R.id.todo_detail_select_alarm_area);
        alarmIcon=findViewById(R.id.todo_detail_alarm_icon);
        selectAlarmTextView=findViewById(R.id.todo_detail_select_alarm_text);
        deleteAlarm=findViewById(R.id.todo_detail_select_alarm_delete);
        CardView noteCardView = findViewById(R.id.todo_detail_note_card_view);

        //Toolbar相关操作
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //状态栏
        changStatusIconColor(true);

        //标题文本框相关
        titleEditText.setText(title);

        //标签文本框相关
        tagEditText.setText(tag);

        //备注文本框相关
        noteEditText.setText(note);
        noteCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteEditText.setFocusable(true);
                noteEditText.setFocusableInTouchMode(true);
                noteEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(noteEditText,0);//TODO 调起软键盘
            }
        });

        //完成复选框相关操作
        if (mode == EDIT_MODE) {//利用待办的数据设置样式
            completeCheckbox.setChecked(isComplete);
            if (isComplete) {
                titleEditText.setTextColor(getResources().getColor(R.color.grey));//文字颜色变成灰色
                titleEditText.setPaintFlags(titleEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
            }
        }
        completeCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (completeCheckbox.isChecked()) {
                    isComplete = true;
                    titleEditText.setTextColor(getResources().getColor(R.color.grey));//文字颜色变成灰色
                    titleEditText.setPaintFlags(titleEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
                    //修改完成时间
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    completeTime = DateUtil.calendarToString(calendar, "yyyy-MM-dd HH:mm:ss");
                } else {
                    isComplete = false;
                    titleEditText.setTextColor(getResources().getColor(R.color.black));//文字颜色变成黑色
                    titleEditText.setPaintFlags(titleEditText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));//取消删除线
                    completeTime = null;//修改完成时间
                }
            }
        });

        //星标复选框相关操作
        if (mode == EDIT_MODE) {//利用待办的数据设置样式
            starCheckbox.setChecked(isStar);
        }
        starCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStar = starCheckbox.isChecked();
            }
        });


        //日期选择相关
        if (mode == EDIT_MODE && dateString != null) {
            selectDateTextView.setText(dateString);
            selectDateTextView.setTextColor(getResources().getColor(R.color.blue));
            calendarIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
            deleteDate.setVisibility(View.VISIBLE);
        }
        //选择日期按钮的点击事件
        selectDateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前条目设定日期的年月日
                //若未设定日期，则使用今天的年月日
                Calendar calendar;
                if (dateString != null) {
                    calendar = DateUtil.stringToCalendar(dateString, "yyyy-MM-dd");
                } else {
                    calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                }
                int calendarYear = calendar.get(Calendar.YEAR);//设定的年份
                int calendarMonth = calendar.get(Calendar.MONTH);//设定的月份，从0开始
                int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);//设定的日期

                //日期选择器对话框相关
                SublimePickerFragment sublimePickerFragment = new SublimePickerFragment();
                Bundle bundle = new Bundle();
                SublimeOptions options = new SublimeOptions();
                options.setCanPickDateRange(false)
                        .setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER)
                        .setDateParams(calendarYear, calendarMonth, calendarDay);
                bundle.putParcelable("SUBLIME_OPTIONS", options);//将option对象放入bundle
                sublimePickerFragment.setArguments(bundle);
                sublimePickerFragment.setCancelable(true);
                //注册事件
                SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
                    @Override
                    public void onCancelled() {
                    }

                    @Override
                    public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                                        int hourOfDay, int minute,
                                                        SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                        String recurrenceRule) {
                        Calendar calendar = selectedDate.getFirstDate();
                        int selectedYear = calendar.get(Calendar.YEAR);
                        int selectedMonth = calendar.get((Calendar.MONTH));//从0开始
                        int selectedDay = calendar.get(Calendar.DATE);
                        dateString = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        selectDateTextView.setText(dateString);
                        selectDateTextView.setTextColor(getResources().getColor(R.color.blue));
                        deleteDate.setVisibility(View.VISIBLE);
                        calendarIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
                    }
                };
                sublimePickerFragment.setCallback(mFragmentCallback);
                FragmentManager fragmentManager = getSupportFragmentManager();
                sublimePickerFragment.show(fragmentManager, "date_picker");
            }
        });

        //删除日期按钮的点击事件
        if(dateString==null){
            deleteDate.setVisibility(View.GONE);
        }
        deleteDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateString = null;
                selectDateTextView.setText(getString(R.string.select_todo_date));
                selectDateTextView.setTextColor(getResources().getColor(R.color.grey));
                deleteDate.setVisibility(View.GONE);
                calendarIcon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
            }
        });

        //闹钟提醒选择相关
        if (mode == EDIT_MODE && alarmString != null) {
            selectAlarmTextView.setText(alarmString);
            selectAlarmTextView.setTextColor(getResources().getColor(R.color.blue));
            alarmIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
            deleteAlarm.setVisibility(View.VISIBLE);
        }
        selectAlarmArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前条目设定提醒的年月日时分
                //若未设定日期，则使用此刻的年月日时分
                Calendar calendar;
                if (alarmString != null) {
                    calendar = DateUtil.stringToCalendar(alarmString, "yyyy-MM-dd HH:mm");
                } else {
                    calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                }
                int calendarYear = calendar.get(Calendar.YEAR);//设定的年份
                int calendarMonth = calendar.get(Calendar.MONTH);//设定的月份，从0开始
                int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);//设定的日
                int calendarHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);//设定的小时，24小时制
                int calendarMinute = calendar.get(Calendar.MINUTE);//设定的分钟
                LogUtil.d("hour_of_day",""+calendarHourOfDay);

                //日期-时间选择器对话框相关
                SublimePickerFragment sublimePickerFragment = new SublimePickerFragment();
                Bundle bundle = new Bundle();
                SublimeOptions options = new SublimeOptions();
                options.setCanPickDateRange(false)
                        .setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER | SublimeOptions.ACTIVATE_TIME_PICKER)
                        .setDateParams(calendarYear, calendarMonth, calendarDay)
                        .setTimeParams(calendarHourOfDay, calendarMinute, true);
                bundle.putParcelable("SUBLIME_OPTIONS", options);//将option对象放入bundle
                sublimePickerFragment.setArguments(bundle);
                sublimePickerFragment.setCancelable(true);
                //注册事件
                SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
                    @Override
                    public void onCancelled() {

                    }

                    @Override
                    public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                                        int hourOfDay, int minute,
                                                        SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                        String recurrenceRule) {
                        Calendar calendar = selectedDate.getFirstDate();
                        int selectedYear = calendar.get(Calendar.YEAR);
                        int selectedMonth = calendar.get((Calendar.MONTH));//从0开始
                        int selectedDay = calendar.get(Calendar.DATE);
                        alarmString = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay + " " + hourOfDay + ":" + minute;
                        selectAlarmTextView.setText(alarmString);
                        selectAlarmTextView.setTextColor(getResources().getColor(R.color.blue));
                        deleteAlarm.setVisibility(View.VISIBLE);
                        alarmIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
                    }
                };
                sublimePickerFragment.setCallback(mFragmentCallback);
                FragmentManager fragmentManager = getSupportFragmentManager();
                sublimePickerFragment.show(fragmentManager, "date_time_picker");
            }
        });

        //删除提醒按钮
        if(alarmString==null){
            deleteAlarm.setVisibility(View.GONE);
        }
        deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmString = null;
                selectAlarmTextView.setText(getString(R.string.select_todo_alarm_time));
                selectAlarmTextView.setTextColor(getResources().getColor(R.color.grey));
                alarmIcon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
                deleteAlarm.setVisibility(View.GONE);
            }
        });
    }

    //加载菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_detail_menu, menu);
        return true;
    }

    //菜单项点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.todo_detail_menu_delete:
                if (mode == EDIT_MODE) {
                    db.execSQL("DELETE FROM TodoList WHERE id = ?", new String[]{"" + id});
                }
                finish();
                break;
            case R.id.todo_detail_menu_view_detail:
                Toast.makeText(this, "You click detail", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //按下返回键
    @Override
    public void onBackPressed() {
        if (mode == EDIT_MODE) {
            if (!TextUtils.isEmpty(titleEditText.getText())) {//TODO 空格也不行、防止sql注入、判断是否有修改以更新修改时间
                title = titleEditText.getText().toString();
                if (!TextUtils.isEmpty(noteEditText.getText())) {
                    note = noteEditText.getText().toString();
                } else {
                    note = null;
                }
                if (!TextUtils.isEmpty(tagEditText.getText())) {
                    tag = tagEditText.getText().toString();
                } else {
                    tag = null;
                }

                ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("is_complete", isComplete);
                values.put("is_star", isStar);
                values.put("note", note);
                values.put("tag", tag);
                values.put("edit_time", editTime);
                values.put("complete_time", completeTime);
                values.put("date", dateString);
                values.put("alarm",alarmString);
                db.update("TodoList", values, "id = ?", new String[]{"" + id});
            }

        } else {//CREATE_MODE
            title = titleEditText.getText().toString();
            if (!TextUtils.isEmpty(titleEditText.getText())) {//TODO 空格也不行、防止sql注入
                title = titleEditText.getText().toString();
                if (!TextUtils.isEmpty(noteEditText.getText())) {
                    note = noteEditText.getText().toString();
                } else {
                    note = null;
                }
                if (!TextUtils.isEmpty(tagEditText.getText())) {
                    tag = tagEditText.getText().toString();
                } else {
                    tag = null;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                createTime = DateUtil.calendarToString(calendar, "yyyy-MM-dd HH:mm:ss");

                ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("is_complete", isComplete);
                values.put("is_star", isStar);
                values.put("note", note);
                values.put("tag", tag);
                values.put("create_time", createTime);
                values.put("complete_time", completeTime);
                values.put("date", dateString);
                values.put("alarm",alarmString);
                db.insert("TodoList", null, values);
            }
        }
        //finish();
        super.onBackPressed();
    }

    public void changStatusIconColor(boolean setDark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            View decorView = getWindow().getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                if(setDark){
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else{
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
}
