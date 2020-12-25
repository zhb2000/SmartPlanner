package com.my.smartplanner.item;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.util.CalendarUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 待办列表子项的实体类
 */
public class TodoListItem implements Serializable {
    private int id;
    private String title;//标题
    private boolean isComplete;//是否已完成
    private boolean isStar;//是否加星
    private Calendar alarm;//闹钟时间
    private String note;//备注
    private Calendar endDate;//设定的日期
    private Calendar createTime;//创建时间
    private Calendar completeTime;//完成时间
    private List<String> tags;


    public TodoListItem(
            int id,
            String title,
            boolean isComplete,
            boolean isStar,
            @Nullable String alarmStr,
            @Nullable String note,
            @Nullable String endDateStr,
            @Nullable String createTimeStr,
            @Nullable String completeTimeStr) {
        this.id = id;
        this.title = title;
        this.isComplete = isComplete;
        this.isStar = isStar;
        this.alarm = CalendarUtil.stringToCalendar(alarmStr, TodoDBHelper.ALARM_PATTERN);
        this.note = note;
        this.endDate = CalendarUtil.stringToCalendar(endDateStr, TodoDBHelper.END_DATE_PATTERN);
        this.createTime = CalendarUtil.stringToCalendar(createTimeStr, TodoDBHelper.CREATE_TIME_PATTERN);
        this.completeTime = CalendarUtil.stringToCalendar(completeTimeStr, TodoDBHelper.COMPLETE_TIME_PATTERN);
        this.tags = new ArrayList<>();
    }

    public TodoListItem() {
        id = -1;
        title = null;
        isComplete = false;
        isStar = false;
        alarm = null;
        note = null;
        endDate = null;
        createTime = null;
        completeTime = null;
        this.tags = new ArrayList<>();
    }

    public void clearTags() {
        tags.clear();
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean getIsComplete() {
        return isComplete;
    }

    public Calendar getAlarm() {
        return alarm;
    }

    public boolean getIsStar() {
        return isStar;
    }

    public String getNote() {
        return note;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public Calendar getCompleteTime() {
        return completeTime;
    }

    public void currentCreate() {
        createTime = Calendar.getInstance();
    }

    /**
     * <p>修改完成状态，若状态与要设置的状态相同则什么都不做</p>
     * <p>修改isComplete和completeTime</p>
     */
    public void currentComplete(boolean isComplete) {
        if (isComplete != this.isComplete) {
            this.isComplete = isComplete;
            completeTime = isComplete ? Calendar.getInstance() : null;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIsStar(boolean isStar) {
        this.isStar = isStar;
    }

    public void setEndDate(@Nullable Calendar endDate) {
        this.endDate = endDate;
    }

    public void setAlarm(@Nullable Calendar alarm) {
        this.alarm = alarm;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public void setTagsByString(@NonNull String tagsStr) {
        tagsStr = tagsStr.trim();
        tags.clear();
        if (!TextUtils.isEmpty(tagsStr)) {
            tags.addAll(Arrays.asList(tagsStr.split("\\s+")));
        }
    }
}
