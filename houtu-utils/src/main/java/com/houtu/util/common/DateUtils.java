package com.houtu.util.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @date 2019年5月31日
 * @author jonlu
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    final static DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    final static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final static DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    final static DateFormat DATE_TIME_UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    final static DateTimeFormatter LOCAL_DATE_TIME_UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        DATE_TIME_UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * 通过date获取时间格式化字符串。示例：2020-01-01
     * @param date 时间对象
     * @return String
     */
    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /**
     * 通过date获取时间格式化字符串。示例：2020-01-01
     * @param date 时间对象
     * @return String
     */
    public static String formatDate(LocalDate date) {
        return LOCAL_DATE_FORMAT.format(date);
    }

    /**
     * 通过date获取时间格式化字符串。示例：2020-01-01 00:00:00
     * @param dateTime 时间对象
     * @return String
     */
    public static String formatDateTime(Date dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }

    /**
     * 通过date获取时间格式化字符串。示例：2020-01-01 00:00:00
     * @param dateTime 时间对象
     * @return String
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return LOCAL_DATE_TIME_FORMAT.format(dateTime);
    }

    /**
     * 通过date获取UTC时间格式化字符串。示例：2020-01-01T00:00:00Z
     * @param date 时间对象
     * @return String
     */
    public static String formatUTCDateTime(Date date) {
        return DATE_TIME_UTC_FORMAT.format(date);
    }

    /**
     * 通过date获取UTC时间格式化字符串。示例：2020-01-01T00:00:00Z
     * @param dateTime 时间对象
     * @return String
     */
    public static String formatUTCDateTime(LocalDateTime dateTime) {
        return LOCAL_DATE_TIME_UTC_FORMAT.format(dateTime);
    }

    /**
     * 通过date获取指定当天开始时间
     * @param date 获取时间对象
     * @return Date
     */
    public static Date toDayStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 通过date获取指定当天开始时间
     * @param dateTime 时间对象
     * @return LocalDateTime
     */
    public static LocalDateTime toDayStart(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN);
    }

    /**
     * 通过date获取指定当天结束时间
     * @param date 指定时间对象
     * @return Date
     */
    public static Date toDayEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 通过date获取指定当天结束时间
     * @param dateTime 时间对象
     * @return LocalDateTime
     */
    public static LocalDateTime toDayEnd(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX);
    }

}
