package com.my.smartplanner.item;

import androidx.annotation.Nullable;

import java.util.Calendar;

/**
 * 待办列表子项的实体类
 */
public class TodoListItem {
    private int idInDatabase;//在数据库中的id
    private String title;//标题
    private boolean isComplete;//是否已完成
    private boolean isStar;//是否加星
    private boolean hasAlarm;//是否设置了提醒
    private String note;//备注
    private Calendar date;//设定的日期


    public TodoListItem(int idInDatabase, String title, boolean isComplete, boolean isStar,
                        boolean hasAlarm, @Nullable String note, @Nullable Calendar date){
        this.idInDatabase = idInDatabase;
        this.title = title;
        this.isComplete = isComplete;
        this.isStar = isStar;
        this.hasAlarm = hasAlarm;
        this.note = note;
        this.date = date;
    }

    public int getIdInDatabase(){
        return idInDatabase;
    }

    public String getTitle(){
        return title;
    }

    public boolean getIsComplete(){
        return isComplete;
    }

    public boolean getHasAlarm(){
        return hasAlarm;
    }

    public boolean getIsStar() {
        return isStar;
    }

    public String getNote(){
        return note;
    }

    public Calendar getDate(){
        return date;
    }

    public int getMonth(){
        return date.get(Calendar.MONTH)+1;
    }

    /*几号*/
    public int getDayOfMonth(){
        return date.get(Calendar.DAY_OF_MONTH);
    }

    /*星期几*/
    public int getDayOfWeek(){
        return date.get(Calendar.DAY_OF_WEEK);
    }

}
