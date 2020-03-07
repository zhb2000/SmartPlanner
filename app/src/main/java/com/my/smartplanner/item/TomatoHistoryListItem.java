package com.my.smartplanner.item;

import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;

/**
 * 番茄钟历史列表项实体类
 */
public class TomatoHistoryListItem {

    public static final int COLOR_MORNING = 0, COLOR_AFTERNOON = 1,
            COLOR_NIGHT = 3, COLOR_UNSUCCESSFUL = -1;

    /**
     * 标题
     */
    private String title;
    /**
     * 是否成功
     */
    private boolean isSuccessful;
    /**
     * 总时长
     */
    private int timeSum;
    /**
     * 工作总时长（分钟）
     */
    private int workSum;
    /**
     * 休息总时长（分钟）
     */
    private int restSum;
    /**
     * 单个工作时长（分钟）
     */
    private int workLen;
    /**
     * 单个休息时长（分钟）
     */
    private int restLen;
    /**
     * 计划的重复次数
     */
    private int clockCnt;
    /**
     * 开始时间
     */
    private Calendar startTime;
    /**
     * 结束时间
     */
    private Calendar endTime;
    private String startTimeStr, endTimeStr;

    /**
     * 列表项中开始时间的字符串
     */
    private String liStartTimeStr;
    /**
     * 列表项中总时长的字符串
     */
    private String liTimeSumStr;
    /**
     * 列表项中已完成/未完成的字符串
     */
    private String liSuccessStr;
    /**
     * 列表项中日期的字符串
     */
    private String liDateStr;
    /**
     * 列表项的颜色
     */
    private int liColor;

    public TomatoHistoryListItem(String title,
                                 boolean isSuccessful,
                                 int timeSum,
                                 int workSum,
                                 int restSum,
                                 int workLen,
                                 int restLen,
                                 int clockCnt,
                                 String startTimeStr,
                                 String endTimeStr) {
        this.title = title;
        this.isSuccessful = isSuccessful;
        this.timeSum = timeSum;
        this.workSum = workSum;
        this.restSum = restSum;
        this.workLen = workLen;
        this.restLen = restLen;
        this.clockCnt = clockCnt;
        this.startTimeStr = startTimeStr;
        this.endTimeStr = endTimeStr;

        startTime = CalendarUtil.stringToCalendar(startTimeStr, "yyyy-MM-dd HH:mm:ss");
        endTime = CalendarUtil.stringToCalendar(endTimeStr, "yyyy-MM-dd HH:mm:ss");

        liStartTimeStr = CalendarUtil.calendarToString(startTime, "HH:mm");
        liTimeSumStr = String.valueOf(timeSum);
        liSuccessStr = isSuccessful ? "已完成" : "未完成";
        liDateStr = (startTime.get(Calendar.MONTH) + 1) + "月" + startTime.get(Calendar.DATE) + "日";

        if (!isSuccessful) {
            liColor = COLOR_UNSUCCESSFUL;
        } else if (CalendarUtil.isTomatoMorning(startTime)) {
            liColor = COLOR_MORNING;
        } else if (CalendarUtil.isTomatoAfternoon(startTime)) {
            liColor = COLOR_AFTERNOON;
        } else {
            liColor = COLOR_NIGHT;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getLiStartTimeStr() {
        return liStartTimeStr;
    }

    public boolean getIsSuccessful() {
        return isSuccessful;
    }

    public int getTimeSum() {
        return timeSum;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public int getClockCnt() {
        return clockCnt;
    }

    public int getRestLen() {
        return restLen;
    }

    public int getRestSum() {
        return restSum;
    }

    public int getWorkLen() {
        return workLen;
    }

    public int getWorkSum() {
        return workSum;
    }

    public String getLiTimeSumStr() {
        return liTimeSumStr;
    }

    public String getLiSuccessStr() {
        return liSuccessStr;
    }

    public String getLiDateStr() {
        return liDateStr;
    }

    public int getLiColor() {
        return liColor;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }
}
