package org.dazao.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日期工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dates {
    private static Logger logger = LoggerFactory.getLogger(Dates.class);

    private static final String[] DEFAULT_PATTERNS = { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd" };

    /**
     * 格式化时间
     * 
     * @param date
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }

        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern);
    }

    /**
     * 格式化时间
     * 
     * @param date
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     * @param zone
     *            时区
     */
    public static String formatDate(Date date, String pattern, TimeZone zone) {
        if (date == null) {
            return "";
        }
        String thisPattern = DEFAULT_PATTERNS[0];
        if (StringUtils.isNotBlank(pattern)) {
            thisPattern = pattern;
        }
        return DateFormatUtils.format(date, thisPattern, zone);
    }

    /**
     * 格式化时间
     * 
     * @param time
     *            待格式化的时间
     * @param pattern
     *            格式化规则
     */
    public static String formatTimeMillis(Long time, String pattern) {
        time = time == null ? 0L : time;
        int len = time.toString().length();
        if (!(len == 13 || len == 10)) {
            return "";
        }

        Date date = null;
        if (len == 13) { // 毫秒
            date = new Date(time);
        } else if (len == 10) {// 秒
            date = new Date(time * 1000);
        }
        return formatDate(date, pattern);
    }

    /**
     * 按照{yyyy-MM-dd}格式化时间
     */
    public static String formatTimeMillisByDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[3]);
    }

    /**
     * 按照{yyyy-MM-dd HH:mm:ss}格式化时间
     */
    public static String formatTimeMillisByFullDatePattern(long times) {
        return formatTimeMillis(times, DEFAULT_PATTERNS[0]);
    }

    /** 根据pattern转换字符串为Date */
    public static Date parse(String source, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /** 根据pattern和时区转换字符串为Date */
    public static Date parse(String source, String pattern, TimeZone timeZone) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timeZone);
            return dateFormat.parse(source);
        } catch (ParseException e) {
            logger.error("dates", e);
        }
        return null;
    }

    /** 根据{@value #DEFAULT_PATTERNS}转换字符串为Date */
    public static Date parse(String source) {
        for (String pattern : DEFAULT_PATTERNS) {
            try {
                return new SimpleDateFormat(pattern).parse(source);
            } catch (ParseException e) {
                continue;
            }
        }
        throw new RuntimeException("找不到适合的pattern");
    }

    /**
     * @see {@link #parse(String, String)}
     */
    public static long parseToTimeMillis(String source, String pattern) {
        return parse(source, pattern).getTime();
    }

    /**
     * @see {@link #parse(String)}
     */
    public static long parseToTimeMillis(String source) {
        return parse(source).getTime();
    }

    /**
     * 获取当天结束时间
     * 
     * @param datestr
     * @return
     */
    public static long endOfDate(String datestr) {
        String today = datestr + " 23:59:59";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 获取当天开始时间
     * 
     * @param datestr
     * @return
     */
    public static long beginOfDate(String datestr) {
        String today = datestr + " 00:00:00";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateformat.parse(today);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            logger.error("dates", e);
            return 0;
        }
    }

    /**
     * 根据时间返回是星期几 0周日 1周一 2周二 3周三 4周四 5 周五6周六
     * 
     * @return
     */
    public static int getWeekMark(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // 获取当前日期是周几
        int week = 0;
        switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
        case GregorianCalendar.SUNDAY:
            week = 0;
            break;
        case GregorianCalendar.MONDAY:
            week = 1;
            break;
        case GregorianCalendar.TUESDAY:
            week = 2;
            break;
        case GregorianCalendar.WEDNESDAY:
            week = 3;
            break;
        case GregorianCalendar.THURSDAY:
            week = 4;
            break;
        case GregorianCalendar.FRIDAY:
            week = 5;
            break;
        case GregorianCalendar.SATURDAY:
            week = 6;
            break;

        default:
            break;
        }
        return week;
    }

    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前日期前一个月日期
     */
    public static long getBeforeByMonth() {
        Date date = new Date();// 当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化对象
        Calendar calendar = Calendar.getInstance();// 日历对象
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);// 月份减一
        return parse(sdf.format(calendar.getTime())).getTime() / 1000;// 输出格式化的日期

    }

    /**
     *
     * 获得指定日期前(后)x天的日期
     *
     * @param date
     *            当前日期
     * @param day
     *            天数（如果day数为负数,说明是此日期前的天数）
     */
    public static long beforNumDay(long time, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(parse(Dates.formatTimeMillis(time, "yyyy-MM-dd HH:mm:ss")));
        c.add(Calendar.DAY_OF_YEAR, day);
        return parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime())).getTime() / 1000;
    }

    public static String formatNow() {
        return formatDate(new Date(), null);
    }

    public static String formatNow(String pattern) {
        return formatDate(new Date(), pattern);
    }

}
