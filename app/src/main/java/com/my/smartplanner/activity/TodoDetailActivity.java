package com.my.smartplanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.fragment.SublimePickerFragment;
import com.my.smartplanner.item.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;

/**
 * 待办条目详情页面的Activity
 */
public class TodoDetailActivity extends AppCompatActivity {

    private static final String OPEN_INTENT_MODE = "mode";
    private static final String OPEN_INTENT_POSITION = "position_in_adapter";
    private static final String OPEN_INTENT_ITEM = "item";

    private static final int CREATE_MODE = 0;//启动方式为创建模式
    private static final int EDIT_MODE = 1;//启动方式为编辑模式


    public static final String RETURN_INTENT_STATUS = "return_status";
    public static final String RETURN_INTENT_POSITION = "list_index";
    public static final String RETURN_INTENT_ITEM = "item";

    public static final int RETURN_STATUS_ADD_NEW = 1;
    public static final int RETURN_STATUS_CHANGE_ITEM = 2;
    public static final int RETURN_STATUS_REMOVE_ITEM = 3;


    private int mode;//模式
    private int listIndex;//该条目在adapter的列表中的下标
    private TodoListItem item;//实体对象

    private Toolbar toolbar;
    private CheckBox completeCheckbox;//完成复选框
    private CheckBox starCheckbox;//星标复选框
    private EditText titleEditText;//标题文本框
    private EditText tagEditText;//标签文本框
    private EditText noteEditText;//备注文本框
    private LinearLayout selectDateArea;//选择日期的区域
    private ImageView calendarIcon;//选择日期区域日历的图标
    private TextView selectDateTextView;//选择日期区域的文字
    private ImageView deleteDate;//删除日期的叉图标
    private LinearLayout selectAlarmArea;//选择提醒的区域
    private ImageView alarmIcon;//提醒图标
    private TextView selectAlarmTextView;//选择提醒区域的文字
    private ImageView deleteAlarm;//删除提醒的叉图标
    private CardView noteCardView;//备注外层的卡片


    /**
     * 以编辑模式启动Activity
     *
     * @param packageContext Context对象
     * @param position       在Adapter中的位置下标
     * @param item           实体对象
     * @param activity       启动者Activity
     * @param requestCode    requestCode
     */
    public static void startTheActivityForResultInEdit(Context packageContext, int position, TodoListItem item,
                                                       Activity activity, int requestCode) {
        Intent intent = new Intent(packageContext, TodoDetailActivity.class);
        intent.putExtra(OPEN_INTENT_MODE, EDIT_MODE);
        intent.putExtra(OPEN_INTENT_POSITION, position);
        intent.putExtra(OPEN_INTENT_ITEM, item);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 以创建模式启动Activity
     *
     * @param packageContext Context对象
     * @param activity       启动者Activity
     * @param requestCode    requestCode
     */
    public static void startTheActivityForResultInCreate(Context packageContext, Activity activity, int requestCode) {
        Intent intent = new Intent(packageContext, TodoDetailActivity.class);
        intent.putExtra(OPEN_INTENT_MODE, CREATE_MODE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 从intent中提取数据，设置以下成员
     * <ul>
     *     <li>mode</li>
     *     <li>listIndex，创建模式下不设置</li>
     *     <li>item，创建模式下new一个</li>
     * </ul>
     */
    private void getTheExtra() {
        Intent intent = getIntent();
        mode = intent.getIntExtra(OPEN_INTENT_MODE, 0);
        if (mode == EDIT_MODE) {
            listIndex = intent.getIntExtra(OPEN_INTENT_POSITION, 0);
            item = (TodoListItem) intent.getSerializableExtra(OPEN_INTENT_ITEM);
        } else {
            item = new TodoListItem();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        getTheExtra();

        findViews();
        toolbarSetting();
        changStatusIconColor(true);

        updateCompleteTitleUI(item.getIsComplete());//标题UI：根据完成状态
        updateDateUI(item.getEndDate());//日期栏UI：根据截止日期
        updateAlarmUI(item.getAlarm());//闹钟栏UI：根据闹钟时间

        setTitleTextAndListener();//标题文本框：文字、监听器
        setCompleteCheckboxCheckedAndListener();//完成复选框：是否选择、监听器
        setNoteTextAndListener();//备注文本框：文字、监听器
        setTagText();//标签文本框：文字
        setStarCheckboxCheckedAndListener();//星标：是否选择、监听器
        setDateAreaListener();//日期栏：监听器
        setDeleteDateListener();//删除日期：监听器
        setAlarmAreaListener();//闹钟栏：监听器
        setDeleteAlarmListener();//删除闹钟：监听器
    }

    /**
     * 加载菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_detail_menu, menu);
        return true;
    }

    /**
     * 菜单项点击事件：返回、删除、详情
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //返回小箭头
            case android.R.id.home:
                onBackPressed();
                break;
            //菜单-删除
            case R.id.todo_detail_menu_delete:
                clickDelete();
                break;
            //菜单-详情
            case R.id.todo_detail_menu_view_detail:
                clickDetail();
                break;
        }
        return true;
    }

    /**
     * 按下返回键，保存数据
     */
    @Override
    public void onBackPressed() {
        if (item.getTitle() != null) {
            item.setTagsByString(tagEditText.getText().toString());
            Intent intent = new Intent();
            if (mode == EDIT_MODE) {
                //编辑模式
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TodoDBHelper.updateRecord(TodoDetailActivity.this, item);
                    }
                }).start();
                intent.putExtra(RETURN_INTENT_STATUS, RETURN_STATUS_CHANGE_ITEM);//修改
                intent.putExtra(RETURN_INTENT_POSITION, listIndex);
                intent.putExtra(RETURN_INTENT_ITEM, item);
            } else {
                //创建模式
                item.currentCreate();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TodoDBHelper.insetRecord(TodoDetailActivity.this, item);
                    }
                }).start();
                intent.putExtra(RETURN_INTENT_STATUS, RETURN_STATUS_ADD_NEW);//新增
            }
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);//无变化
        }
        super.onBackPressed();
    }

    /**
     * 点击删除按钮时触发
     */
    private void clickDelete() {
        if (mode == EDIT_MODE) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.confirm_to_delete));
            dialog.setMessage(R.string.confirm_to_delete_todo);
            dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TodoDBHelper.deleteRecord(TodoDetailActivity.this, item.getId());
                        }
                    }).start();
                    Intent intent = new Intent();
                    intent.putExtra(RETURN_INTENT_STATUS, RETURN_STATUS_REMOVE_ITEM);//删除
                    intent.putExtra(RETURN_INTENT_POSITION, listIndex);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.show();
        } else {
            finish();
        }
    }

    /**
     * 点击详情按钮时触发
     */
    private void clickDetail() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.view_detail));
        StringBuilder message = new StringBuilder();
        String createTimeStr = item.getCreateTime() == null
                ? getString(R.string.not_created_yet)
                : CalendarUtil.calendarToString(item.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
        String completeTimeStr = item.getCompleteTime() == null
                ? getString(R.string.not_completed_yet)
                : CalendarUtil.calendarToString(item.getCompleteTime(), "yyyy-MM-dd HH:mm:ss");
        message.append(getString(R.string.create_time)).append("：").append(createTimeStr)
                .append("\n")
                .append(getString(R.string.complete_time)).append("：").append(completeTimeStr);
        dialog.setMessage(message);
        dialog.show();
    }


    /**
     * 修改完成状态
     * <ul>
     *     <li>修改item对象的isComplete和completeTime属性</li>
     *     <li>UI更改：完成复选框、标题文字</li>
     * </ou>
     *
     * @param isComplete 完成状态
     */
    private void setCompleteStatus(boolean isComplete) {
        item.currentComplete(isComplete);
        updateCompleteTitleUI(isComplete);
    }

    /**
     * 修改截止日期状态
     * <ul>
     *     <li>修改item对象的endDate属性</li>
     *     <li>日期栏UI更改</li>
     * </ul>
     *
     * @param endDate 截止日期
     */
    private void setEndDateStatus(@Nullable Calendar endDate) {
        item.setEndDate(endDate);
        updateDateUI(endDate);
    }

    /**
     * 修改闹钟时间状态
     * <ul>
     *     <li>修改item对象的alarm属性</li>
     *     <li>闹钟栏UI更改</li>
     * </ul>
     *
     * @param alarm 闹钟时间
     */
    private void setAlarmStatus(@Nullable Calendar alarm) {
        item.setAlarm(alarm);
        updateAlarmUI(alarm);
    }


    /**
     * 根据完成状态设置标题的UI
     */
    private void updateCompleteTitleUI(boolean isComplete) {
        if (isComplete) {
            titleEditText.setTextColor(getResources().getColor(R.color.grey));//文字颜色变成灰色
            titleEditText.setPaintFlags(titleEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
        } else {
            titleEditText.setTextColor(getResources().getColor(R.color.black));//文字颜色变成黑色
            titleEditText.setPaintFlags(titleEditText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));//取消删除线
        }
    }

    /**
     * 根据截止日期设置日期栏的UI
     */
    private void updateDateUI(@Nullable Calendar endDate) {
        if (endDate != null) {
            selectDateTextView.setText(CalendarUtil.calendarToString(
                    endDate, "yyyy-MM-dd"));//文本设置为日期
            deleteDate.setVisibility(View.VISIBLE);//显示叉按钮
            if (CalendarUtil.dateLocation(endDate) == CalendarUtil.DATE_LOCATION_YESTERDAY
                    || CalendarUtil.dateLocation(endDate) == CalendarUtil.DATE_LOCATION_LONG_BEFORE) {
                //截止日期是以前
                selectDateTextView.setTextColor(getResources().getColor(R.color.red));//红色文字
                calendarIcon.setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);//红色日历图标
            } else {
                //截止日期是今天或以后
                selectDateTextView.setTextColor(getResources().getColor(R.color.blue));//蓝色文字
                calendarIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);//蓝色日历图标
            }
        } else {
            selectDateTextView.setText(getString(R.string.select_todo_date));//文本设置为提示文字
            deleteDate.setVisibility(View.GONE);//不显示叉按钮
            selectDateTextView.setTextColor(getResources().getColor(R.color.grey));//灰色文字
            calendarIcon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);//灰色日历图标
        }
    }

    /**
     * 根据闹钟时间设置闹钟栏的UI
     */
    private void updateAlarmUI(@Nullable Calendar alarm) {
        if (alarm != null) {
            selectAlarmTextView.setText(CalendarUtil.calendarToString(
                    alarm, "yyyy-MM-dd HH:mm"));//文本显示闹钟时间
            deleteAlarm.setVisibility(View.VISIBLE);//显示叉按钮
            if (CalendarUtil.dateLocation(alarm) == CalendarUtil.DATE_LOCATION_YESTERDAY
                    || CalendarUtil.dateLocation(alarm) == CalendarUtil.DATE_LOCATION_LONG_BEFORE) {
                //闹钟时间在以前
                selectAlarmTextView.setTextColor(getResources().getColor(R.color.red));//红色文字
                alarmIcon.setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);//红色闹钟图标
            } else {
                //闹钟时间在今天或之后
                selectAlarmTextView.setTextColor(getResources().getColor(R.color.blue));//蓝色文字
                alarmIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);//蓝色闹钟图标
            }
        } else {
            selectAlarmTextView.setText(getString(R.string.select_todo_alarm_time));//文本显示提示语
            deleteAlarm.setVisibility(View.GONE);//不显示叉按钮
            selectAlarmTextView.setTextColor(getResources().getColor(R.color.grey));//灰色文字
            alarmIcon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);//灰色闹钟图标
        }
    }


    /**
     * 获取控件实例
     */
    private void findViews() {
        toolbar = findViewById(R.id.todo_detail_toolbar);
        titleEditText = findViewById(R.id.todo_detail_title_edit_text);
        tagEditText = findViewById(R.id.todo_detail_tag_edit_text);
        noteEditText = findViewById(R.id.todo_detail_note_edit_text);
        completeCheckbox = findViewById(R.id.todo_detail_complete);
        starCheckbox = findViewById(R.id.todo_detail_star);
        selectDateArea = findViewById(R.id.todo_detail_select_date_area);
        calendarIcon = findViewById(R.id.todo_detail_calendar_icon);
        selectDateTextView = findViewById(R.id.todo_detail_select_date_text);
        deleteDate = findViewById(R.id.todo_detail_select_date_delete);
        selectAlarmArea = findViewById(R.id.todo_detail_select_alarm_area);
        alarmIcon = findViewById(R.id.todo_detail_alarm_icon);
        selectAlarmTextView = findViewById(R.id.todo_detail_select_alarm_text);
        deleteAlarm = findViewById(R.id.todo_detail_select_alarm_delete);
        noteCardView = findViewById(R.id.todo_detail_note_card_view);
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
     * 尝试把状态栏图标变成黑色
     */
    public void changStatusIconColor(boolean setDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if (setDark) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
    }

    /**
     * 标题文本框，设置文字，设置监听器
     */
    private void setTitleTextAndListener() {
        titleEditText.setText(item.getTitle());

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String title = s.toString();
                if (TextUtils.isEmpty(title.trim())) {
                    title = null;
                }
                item.setTitle(title);
            }
        });
    }

    /**
     * 备注文本框，设置里面的文字，添加监听器
     */
    private void setNoteTextAndListener() {
        noteEditText.setText(item.getNote());

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String note = s.toString();
                if (TextUtils.isEmpty(note.trim())) {
                    note = null;
                }
                item.setNote(note);
            }
        });

        noteCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteEditText.setFocusable(true);
                noteEditText.setFocusableInTouchMode(true);
                noteEditText.requestFocus();
                //调起软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(noteEditText, 0);
                }
            }
        });
    }

    /**
     * 标签文本框，设置文字
     */
    private void setTagText() {
        StringBuilder tagStr = new StringBuilder();
        boolean isFirst = true;
        for (String tag : item.getTags()) {
            if (isFirst) {
                isFirst = false;
            } else {
                tagStr.append(" ");
            }
            tagStr.append(tag);
        }
        tagEditText.setText(tagStr);
    }

    /**
     * 完成复选框，添加监听器
     */
    private void setCompleteCheckboxCheckedAndListener() {
        completeCheckbox.setChecked(item.getIsComplete());
        completeCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCompleteStatus(completeCheckbox.isChecked());
            }
        });
    }

    /**
     * 星标复选框，设置是否选择，设置监听器
     */
    private void setStarCheckboxCheckedAndListener() {
        starCheckbox.setChecked(item.getIsStar());
        starCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setIsStar(starCheckbox.isChecked());
            }
        });
    }

    /**
     * 设置日期栏的点击事件
     */
    private void setDateAreaListener() {
        selectDateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前条目的截止日期，若没有截止日期日期，则使用今天
                Calendar calendar = item.getEndDate() == null ? Calendar.getInstance() : item.getEndDate();
                int calendarYear = calendar.get(Calendar.YEAR);//设定的年份
                int calendarMonth = calendar.get(Calendar.MONTH);//设定的月份，从0开始
                int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);//设定的日期

                //日期选择器对话框相关
                SublimePickerFragment sublimePickerFragment = new SublimePickerFragment();
                Bundle bundle = new Bundle();
                SublimeOptions options = new SublimeOptions();//option对象
                options.setCanPickDateRange(false)
                        .setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER)
                        .setDateParams(calendarYear, calendarMonth, calendarDay);
                bundle.putParcelable("SUBLIME_OPTIONS", options);//将option对象放入bundle
                sublimePickerFragment.setArguments(bundle);
                sublimePickerFragment.setCancelable(true);
                //给Callback对象注册事件
                SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
                    //对话框被取消
                    @Override
                    public void onCancelled() {
                    }

                    //选择了一个日期
                    @Override
                    public void onDateTimeRecurrenceSet(
                            SelectedDate selectedDate,
                            int hourOfDay, int minute,
                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                            String recurrenceRule) {
                        setEndDateStatus(selectedDate.getFirstDate());
                    }
                };
                sublimePickerFragment.setCallback(mFragmentCallback);
                FragmentManager fragmentManager = getSupportFragmentManager();
                sublimePickerFragment.show(fragmentManager, "date_picker");
            }
        });
    }

    /**
     * 设置删除日期按钮的监听器
     */
    private void setDeleteDateListener() {
        deleteDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEndDateStatus(null);
            }
        });
    }

    /**
     * 设置闹钟栏的点击事件
     */
    private void setAlarmAreaListener() {
        selectAlarmArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前条目设定闹钟，若未设定闹钟，则使用此刻
                Calendar calendar = item.getAlarm() == null ? Calendar.getInstance() : item.getAlarm();
                int calendarYear = calendar.get(Calendar.YEAR);//设定的年份
                int calendarMonth = calendar.get(Calendar.MONTH);//设定的月份，从0开始
                int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);//设定的日
                int calendarHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);//设定的小时，24小时制
                int calendarMinute = calendar.get(Calendar.MINUTE);//设定的分钟

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
                //给Callback注册事件
                SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
                    //对话框被取消
                    @Override
                    public void onCancelled() {
                    }

                    //选择了一个时间
                    @Override
                    public void onDateTimeRecurrenceSet(
                            SelectedDate selectedDate,
                            int hourOfDay, int minute,
                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                            String recurrenceRule) {
                        Calendar alarm = selectedDate.getFirstDate();
                        alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarm.set(Calendar.MINUTE, minute);
                        setAlarmStatus(alarm);
                    }
                };
                sublimePickerFragment.setCallback(mFragmentCallback);
                FragmentManager fragmentManager = getSupportFragmentManager();
                sublimePickerFragment.show(fragmentManager, "date_time_picker");
            }
        });
    }

    /**
     * 设置删除闹钟按钮的监听器
     */
    private void setDeleteAlarmListener() {
        deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarmStatus(null);
            }
        });
    }
}
