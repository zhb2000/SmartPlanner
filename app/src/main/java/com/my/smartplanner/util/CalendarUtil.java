package com.my.smartplanner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换等工具
 */
public class CalendarUtil {
    public static final int DATE_LOCATION_YESTERDAY = -1;
    public static final int DATE_LOCATION_TODAY = 0;
    public static final int DATE_LOCATION_TOMORROW = 1;
    public static final int DATE_LOCATION_LONG_BEFORE = -2;
    public static final int DATE_LOCATION_LONG_AFTER = 2;

    /**
     * 检测某个日期相对于今天的关系
     *
     * @param calendar 需要检测的日期
     * @return 该日期是昨天，返回-1；是今天，返回0；是明天，返回1；
     * 都不是且在之前，返回-2；都不是且在之后，返回2
     */
    public static int dateLocation(Calendar calendar) {
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

    public static final int DATE_COMPARE_SAME = 0;
    public static final int DATE_COMPARE_AFTER = 1;
    public static final int DATE_COMPARE_BEFORE = -1;
    /**
     * 比较两个日期的先后
     *
     * @return
     * date1在后，返回1，相同，返回0，date1在前，返回-1。只比较到日
     * */
    public static int dateCompare(Calendar date1, Calendar date2) {
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
     * @param pattern 指定格式化方式
     * @return 转换出的String字符串
     * */
    public static String calendarToString(Calendar calendar, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(calendar.getTime());
    }

    /**
     * String转换成Calendar
     *
     * @param string 被转换的String字符串
     * @param pattern 指定格式化方式
     * @return 转换出的Calendar对象
     * */
    public static Calendar stringToCalendar(String string, String pattern) {
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
}
