package com.my.smartplanner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /*是昨天，返回-1；是今天，返回0；是明天，返回1；都不是且在之前，返回-2；都不是且在之后，返回2*/
    public static int dateLocation(Calendar calendar) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(new Date());
        Calendar yesterdayCalendar = Calendar.getInstance();
        yesterdayCalendar.setTime(new Date());
        yesterdayCalendar.add(Calendar.DATE, -1);
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.setTime(new Date());
        tomorrowCalendar.add(Calendar.DATE, 1);

        if (dateCompare(calendar, todayCalendar) == 0)
            return 0;
        if (dateCompare(calendar, yesterdayCalendar) == 0)
            return -1;
        switch (dateCompare(calendar, todayCalendar)) {
            case 0:
                return 0;
            case 1:
                return 2;
            case -1:
                return -2;
            default://消waring
                return 2;
        }
    }

    /*date1在后，返回1，相同，返回0，date1在前，返回-1。只比较到日*/
    public static int dateCompare(Calendar date1, Calendar date2) {
        int day1 = date1.get(Calendar.DATE);
        int day2 = date2.get(Calendar.DATE);
        int month1 = date1.get(Calendar.MONTH);
        int month2 = date2.get(Calendar.MONTH);
        int year1 = date1.get(Calendar.YEAR);
        int year2 = date2.get(Calendar.YEAR);
        if (year1 > year2)
            return 1;
        if (year1 < year2)
            return -1;
        if (month1 > month2)
            return 1;
        if (month1 < month2)
            return -1;
        if (day1 > day2)
            return 1;
        if (day1 < day2)
            return -1;
        return 0;
    }

    /*Calendar对象转换成String*/
    public static String calendarToString(Calendar calendar, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(calendar.getTime());
    }

    /*String转换成Calendar*/
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
