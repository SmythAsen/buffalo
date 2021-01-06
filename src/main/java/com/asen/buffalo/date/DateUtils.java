package com.asen.buffalo.date;

import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 时间相关工具类
 *
 * @author Asen
 * @since 1.0.0
 */
public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD);
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(HH_MM_SS);
    private static final Map<String, DateTimeFormatter> PATTEN_FORMATTER_MAPPER = new HashMap<>();

    static {
        PATTEN_FORMATTER_MAPPER.put(YYYY_MM_DD_HH_MM_SS, DEFAULT_DATE_TIME_FORMATTER);
        PATTEN_FORMATTER_MAPPER.put(YYYY_MM_DD, DEFAULT_DATE_FORMATTER);
        PATTEN_FORMATTER_MAPPER.put(HH_MM_SS, DEFAULT_TIME_FORMATTER);
    }

    private static DateTimeFormatter cacheFormatterAndGet(String pattern) {
        DateTimeFormatter dateTimeFormatter = PATTEN_FORMATTER_MAPPER.get(pattern);
        if (dateTimeFormatter == null) {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
            PATTEN_FORMATTER_MAPPER.put(pattern, dateTimeFormatter);
        }
        return dateTimeFormatter;
    }

    /**
     * 默认格式化{@code LocalDateTime},使用格式{@value YYYY_MM_DD_HH_MM_SS}
     * default format{@code LocalDateTime} using {@value YYYY_MM_DD_HH_MM_SS}
     *
     * @param localDateTime date time
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return LocalDateTime.now().format(DEFAULT_DATE_TIME_FORMATTER);
        }
        return localDateTime.format(DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * 根据pattern格式化{@code LocalDateTime}
     * format {@code LocalDateTime} using pattern
     *
     * @param localDateTime time
     * @param pattern       using pattern
     * @return pattern's time
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return formatLocalDateTime(localDateTime);
        }
        DateTimeFormatter dateTimeFormatter = cacheFormatterAndGet(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 默认格式化{@code LocalDate}，使用格式{@value YYYY_MM_DD}
     * default format{@code LocalDate} using {@value YYYY_MM_DD}
     *
     * @param localDate localDate
     * @return yyyy-MM-dd
     */
    public static String formatLocalDate(LocalDate localDate) {
        if (Objects.isNull(localDate)) {
            return LocalDate.now().format(DEFAULT_DATE_FORMATTER);
        }
        return localDate.format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 根据pattern格式化{@code LocalDate}
     * format {@code LocalDate} using pattern
     *
     * @param localDate date
     * @param pattern   only support date pattern,example {@value YYYY_MM_DD} or {@value YYYYMMDD}
     * @return yyyy-MM-dd
     */
    public static String formatLocalDate(LocalDate localDate, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return formatLocalDate(localDate);
        }
        DateTimeFormatter dateTimeFormatter = cacheFormatterAndGet(pattern);
        return localDate.format(dateTimeFormatter);
    }

    /**
     * 默认格式化{@code localTime}，使用格式{@value HH_MM_SS}
     * default format{@code LocalDate} using {@value HH_MM_SS}
     *
     * @param localTime localTime
     * @return HH:mm:ss
     */
    public static String formatLocalTime(LocalTime localTime) {
        if (Objects.isNull(localTime)) {
            return LocalTime.now().format(DEFAULT_TIME_FORMATTER);
        }
        return localTime.format(DEFAULT_TIME_FORMATTER);
    }

    /**
     * 根据pattern格式化{@code LocalTime}
     * format {@code LocalTime} using pattern
     *
     * @param localTime localTime
     * @param pattern   only support time pattern, example {@value HH_MM_SS}
     * @return HH:mm:ss
     */
    public static String formatLocalTime(LocalTime localTime, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return formatLocalTime(localTime);
        }
        DateTimeFormatter dateTimeFormatter = cacheFormatterAndGet(pattern);
        return localTime.format(dateTimeFormatter);
    }

    /**
     * 根据pattern格式化{@code date}
     * format {@code date} using pattern
     *
     * @param date    date time
     * @param pattern pattern
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String format(Date date, String pattern) {
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(cacheFormatterAndGet(pattern));
    }

    /**
     * 默认格式化{@code Date}，使用格式{@value YYYY_MM_DD_HH_MM_SS}
     * default format {@code Date} using pattern {@value YYYY_MM_DD_HH_MM_SS}
     *
     * @param date date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String format(Date date) {
        return format(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 默认格式化{@code Date}，使用格式{@value YYYY_MM_DD}
     * default format {@code Date} using pattern {@value YYYY_MM_DD}
     *
     * @param date date
     * @return yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        return format(date, YYYY_MM_DD);
    }

    /**
     * 默认格式化{@code Date}，使用格式{@value HH_MM_SS}
     * default format {@code Date} using pattern {@value HH_MM_SS}
     *
     * @param date date
     * @return HH:mm:ss
     */
    public static String formatTime(Date date) {
        return format(date, HH_MM_SS);
    }

    /**
     * 根据pattern格式化{@code mills}
     * format {@code mills} using pattern
     *
     * @param pattern pattern
     * @param mills   mills
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatMills(long mills, String pattern) {
        Instant instant = Instant.ofEpochMilli(mills);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return formatLocalDateTime(localDateTime, pattern);
    }

    /**
     * 默认格式化{@code mills}，使用格式{@value YYYY_MM_DD_HH_MM_SS}
     * default format {@code mills} using pattern {@value YYYY_MM_DD_HH_MM_SS}
     *
     * @param mills mills
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatMills(long mills) {
        return formatMills(mills, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 默认格式化{@code mills}，使用格式{@value YYYY_MM_DD}
     * default format {@code mills} using pattern {@value YYYY_MM_DD}
     *
     * @param mills mills
     * @return yyyy-MM-dd
     */
    public static String formatMillsDate(long mills) {
        return formatMills(mills, YYYY_MM_DD);
    }

    /**
     * 默认格式化{@code mills}，使用格式{@value HH_MM_SS}
     * default format {@code mills} using pattern {@value HH_MM_SS}
     *
     * @param mills 时间戳
     * @return HH:mm:ss
     */
    public static String formatMillsTime(long mills) {
        return formatMills(mills, HH_MM_SS);
    }

    /**
     * 将字符串类型的时间解析为{@link Date}类型的时间
     * Parse a string type datetime into a datetime of type {@link Date}
     *
     * @param date yyyy-MM-dd HH:mm:ss
     * @return Date
     */
    public static Date parse(String date) {
        LocalDateTime localDateTime = parseToLocalDateTime(date);
        Instant instant = localDateTime.toInstant(OffsetDateTime.now().getOffset());
        return Date.from(instant);
    }

    /**
     * 根据pattern格式化{@code date}
     * format {@code date} using pattern
     *
     * @param date    string date
     * @param pattern formatter pattern
     * @return LocalDateTime
     */
    public static LocalDateTime parseToLocalDateTime(String date, String pattern) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将字符串类型的时间解析为{@link LocalDateTime}类型的时间
     * Parse a string type datetime into a datetime of type {@link LocalDateTime}
     *
     * @param date yyyy-MM-dd HH:mm:ss
     * @return LocalDateTime
     */
    public static LocalDateTime parseToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * 将字符串类型的时间解析为{@link LocalDate}类型的时间
     * Parse a string type date into a datetime of type {@link LocalDate}
     *
     * @param date yyyy-MM-dd
     * @return LocalDate
     */
    public static LocalDate parseToLocalDate(String date) {
        return LocalDate.parse(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * 将字符串类型的时间解析为{@link LocalTime}类型的时间
     * Parse a string type date into a time of type {@link LocalTime}
     *
     * @param date HH:mm:ss
     * @return LocalTime
     */
    public static LocalTime parseToLocalTime(String date) {
        return LocalTime.parse(date, DEFAULT_TIME_FORMATTER);
    }

    /**
     * 计算两个时间的间隔时期
     * Calculate the number of days between two dates
     *
     * @param dayStart date
     * @param dayEnd   date
     * @return Period
     */
    public static Period betweenDays(Date dayStart, Date dayEnd) {
        LocalDateTime localDateTimeStart = LocalDateTime.ofInstant(dayStart.toInstant(), OffsetDateTime.now().getOffset());
        LocalDateTime localDateTimeEnd = LocalDateTime.ofInstant(dayEnd.toInstant(), OffsetDateTime.now().getOffset());
        return Period.between(localDateTimeStart.toLocalDate(), localDateTimeEnd.toLocalDate());
    }

    /**
     * 计算两个时间的间隔时间
     * Calculate the number of days between two times
     *
     * @param timeStart date
     * @param timeEnd   date
     * @return Duration
     */
    public static Duration betweenTimes(Date timeStart, Date timeEnd) {
        LocalDateTime localDateTimeStart = LocalDateTime.ofInstant(timeStart.toInstant(), OffsetDateTime.now().getOffset());
        LocalDateTime localDateTimeEnd = LocalDateTime.ofInstant(timeEnd.toInstant(), OffsetDateTime.now().getOffset());
        return Duration.between(localDateTimeStart, localDateTimeEnd);
    }

    /**
     * 计算两个时间的间隔时期
     * Calculate the number of days between two dates
     *
     * @param dayStart date
     * @param dayEnd   date
     * @return Period
     */
    public static Period betweenDays(String dayStart, String dayEnd) {
        return Period.between(parseToLocalDate(dayStart), parseToLocalDate(dayEnd));
    }

    /**
     * 计算两个时间的间隔时间
     * Calculate the number of days between two times
     *
     * @param timeStart date
     * @param timeEnd   date
     * @return Duration
     */
    public static Duration betweenTimes(String timeStart, String timeEnd) {
        return Duration.between(parseToLocalDateTime(timeStart), parseToLocalDateTime(timeEnd));
    }

    /**
     * 获取两个时间之间的周末天数
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 周末天数
     */
    public static int getWeekDays(LocalDateTime start, LocalDateTime end) {
        Duration between = Duration.between(start, end);
        return getWeekDays(start, between.toDays());
    }

    /**
     * 获取时间点新增天数中的周末天数
     *
     * @param start    开始时间
     * @param plusDays 新增天数
     * @return 周末天数
     */
    public static int getWeekDays(LocalDateTime start, long plusDays) {
        if (plusDays <= 0) {
            return 0;
        }
        int result = 0;
        LocalDateTime endDay;
        for (int day = 0; day <= plusDays; day++) {
            endDay = start.plusDays(day);
            if (DayOfWeek.SATURDAY.equals(endDay.getDayOfWeek())
                    || DayOfWeek.SUNDAY.equals(endDay.getDayOfWeek())) {
                result++;
            }
        }
        return result;
    }

    /**
     * 获取本周开始时间
     * get start date of the {@code date} in week
     *
     * @param date calculate date
     * @return start of the week date
     */
    public static Date startWeekDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayWeek == 1) {
            dayWeek = 7;
        } else {
            dayWeek -= 1;
        }
        cal.add(Calendar.DAY_OF_MONTH, 1 - dayWeek);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}