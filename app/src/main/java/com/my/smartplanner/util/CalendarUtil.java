package com.my.smartplanner.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换等工具
 */
public class CalendarUtil {
    //检测日期先后顺序
    /**
     * 返回值：日期是昨天
     */
    public static final int DATE_LOCATION_YESTERDAY = -1;
    /**
     * 返回值：日期是今天
     */
    public static final int DATE_LOCATION_TODAY = 0;
    /**
     * 返回值：日期是明天
     */
    public static final int DATE_LOCATION_TOMORROW = 1;
    /**
     * 返回值：日期比昨天还要早
     */
    public static final int DATE_LOCATION_LONG_BEFORE = -2;
    /**
     * 返回值：日期比明天还要晚
     */
    public static final int DATE_LOCATION_LONG_AFTER = 2;

    /**
     * 检测某个日期相对于今天的关系
     *
     * @param calendar 需要检测的日期
     * @return <p>比昨天还要早: DATE_LOCATION_LONG_BEFORE</p>
     * <p>昨天: DATE_LOCATION_YESTERDAY</p>
     * <p>今天: DATE_LOCATION_TODAY</p>
     * <p>明天: DATE_LOCATION_TOMORROW</p>
     * <p>比明天还要晚: DATE_LOCATION_LONG_AFTER</p>
     */
    public static int dateLocation(@NonNull Calendar calendar) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(new Date());
        Calendar yesterdayCalendar = Calendar.getInstance();
        yesterdayCalendar.setTime(new Date());
        yesterdayCalendar.add(Calendar.DATE, -1);
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.setTime(new Date());
        tomorrowCalendar.add(Calendar.DATE, 1);

        //与明天比较
        if (dateCompare(calendar, tomorrowCalendar) == DATE_COMPARE_SAME)
            return DATE_LOCATION_TOMORROW;
        //与昨天比较
        if (dateCompare(calendar, yesterdayCalendar) == DATE_COMPARE_SAME)
            return DATE_LOCATION_YESTERDAY;
        //与今天比较
        switch (dateCompare(calendar, todayCalendar)) {
            case DATE_COMPARE_SAME:
                return DATE_LOCATION_TODAY;
            case DATE_COMPARE_AFTER:
                return DATE_LOCATION_LONG_AFTER;
            case DATE_COMPARE_BEFORE:
                return DATE_LOCATION_LONG_BEFORE;
            default://消waring
                return 2;
        }
    }


    //比较日期是否同一日
    /**
     * 返回值：相同
     */
    public static final int DATE_COMPARE_SAME = 0;
    /**
     * 返回值：date1在前，date2在后
     */
    public static final int DATE_COMPARE_AFTER = 1;
    /**
     * 返回值：date2在前，date1在后
     */
    public static final int DATE_COMPARE_BEFORE = -1;

    /**
     * 比较两个日期的先后，只比较到日
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return <p>相同: DATE_COMPARE_SAME</p>
     * <p>date1在前，date2在后: DATE_COMPARE_AFTER</p>
     * <p>date2在前，date1在后: DATE_COMPARE_BEFORE</p>
     */
    public static int dateCompare(@NonNull Calendar date1, @NonNull Calendar date2) {
        int day1 = date1.get(Calendar.DATE);
        int day2 = date2.get(Calendar.DATE);
        int month1 = date1.get(Calendar.MONTH);
        int month2 = date2.get(Calendar.MONTH);
        int year1 = date1.get(Calendar.YEAR);
        int year2 = date2.get(Calendar.YEAR);
        if (year1 > year2)
            return DATE_COMPARE_AFTER;
        if (year1 < year2)
            return DATE_COMPARE_BEFORE;
        if (month1 > month2)
            return DATE_COMPARE_AFTER;
        if (month1 < month2)
            return DATE_COMPARE_BEFORE;
        if (day1 > day2)
            return DATE_COMPARE_AFTER;
        if (day1 < day2)
            return DATE_COMPARE_BEFORE;
        return DATE_COMPARE_SAME;
    }


    /**
     * Calendar对象转换成String
     *
     * @param calendar 被转换的Calendar对象
     * @param pattern  指定格式化方式
     * @return 转换出的String字符串
     */
    public static String calendarToString(@Nullable Calendar calendar, @NonNull String pattern) {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(calendar.getTime());
    }


    /**
     * String转换成Calendar
     *
     * @param string  被转换的String字符串
     * @param pattern 指定格式化方式
     * @return 转换出的Calendar对象
     */
    public static Calendar stringToCalendar(@Nullable String string, @NonNull String pattern) {
        if (string == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return calendar;
    }


    /**
     * 把分钟转换为毫秒
     *
     * @param minute 分钟数
     * @return 毫秒数
     */
    public static long minuteToMillisecond(int minute) {
        return minute * 60 * 1000;
    }


    /**
     * <p>该时间是否是早上(6:00~13:00)</p>
     * <p>在番茄钟列表中使用</p>
     */
    public static boolean isTomatoMorning(Calendar calendar) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return hourOfDay >= 6 && hourOfDay < 13;
    }


    /**
     * <p>该时间是否是下午(13:00~18:00)</p>
     * <p>在番茄钟列表中使用</p>
     */
    public static boolean isTomatoAfternoon(Calendar calendar) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return hourOfDay >= 13 && hourOfDay < 18;
    }


    /**
     * <p>该时间是否是晚上(18:00~6:00)</p>
     * <p>在番茄钟列表中使用</p>
     */
    public static boolean isTomatoNight(Calendar calendar) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return hourOfDay >= 18 || hourOfDay < 6;
    }


}
