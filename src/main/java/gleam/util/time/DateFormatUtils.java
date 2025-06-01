package gleam.util.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import gleam.config.ServerSettings;
import gleam.config.define.Languages;

/**
 * 时间格式化工具类<br>
 * 时间->字符串<br>
 * 字符串->时间<br>
 * 
 * @author redback
 * @version 1.00
 * @time 2020-6-11 18:01
 */
public class DateFormatUtils {

    /**
     * 年-月-日 时:分:秒
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 年-月-日
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 日-月-年<br>
     * 越南的日期格式
     */
    public static final String VIETNAME_DATE_PATTERN = "dd-MM-yyyy";

    /**
     * 年-月-日 时:分:秒
     * 
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    /**
     * 年-月-日
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    /**
     * 越南的日期格式 日-月-年
     */
    public static final DateTimeFormatter VIETNAME_DATE_FORMATTER = DateTimeFormatter.ofPattern(VIETNAME_DATE_PATTERN);

    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<>();

    /**
     * 格式化时间<br>
     * 转为年-月-日时:分:秒
     * 
     * @param date
     * @return
     */
    public static String format(Date date) {
        return getFullDateFormat().format(date);
    }

    /**
     * 自定义格式时间
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 格式化时间<br>
     * 转为年-月-日 时:分:秒
     * 
     * @param instant
     * @return
     */
    public static String format(Instant instant) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return zonedDateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 自定义格式化时间
     * 
     * @param instant
     * @param formatter
     * @return
     */
    public static String format(Instant instant, DateTimeFormatter formatter) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        String timeStr = zonedDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 自定义格式化时间
     *
     * @param instant
     * @param pattern 自定义格式
     * @return
     */
    public static String format(Instant instant, String pattern) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String timeStr = zonedDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 格式化时间<br>
     * 年-月-日
     * 
     * @param localDate
     * @return
     */
    public static String format(LocalDate localDate) {
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间<br>
     * 转为年-月-日 时:分:秒
     * 
     * @param localDateTime
     * @return
     */
    public static String format(LocalDateTime localDateTime) {
        return localDateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 自定义格式时间
     * 
     * @param localDateTime
     * @param formatter
     * @return
     */
    public static String format(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        String timeStr = localDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 自定义格式化时间
     *
     * @param localDateTime
     * @param pattern       自定义格式
     * @return
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String timeStr = localDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 格式化时间<br>
     * 转为年-月-日 时:分:秒
     * 
     * @param time
     * @return
     */
    public static String format(long time) {
        ZonedDateTime dateTime = timestamp2DateTime(time);
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 自定义格式化时间
     * 
     * @param time
     * @param formatter
     * @return
     */
    public static String format(long time, DateTimeFormatter formatter) {
        ZonedDateTime dateTime = timestamp2DateTime(time);
        String timeStr = dateTime.format(formatter);
        return timeStr;
    }

    /**
     * 自定义格式化时间
     *
     * @param time
     * @param pattern 自定义格式
     * @return
     */
    public static String format(long time, String pattern) {
        ZonedDateTime dateTime = timestamp2DateTime(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String timeStr = dateTime.format(formatter);
        return timeStr;
    }

    /**
     * 格式化时间<br>
     * 转为年-月-日 时:分:秒
     * 
     * @param zonedDateTime
     * @return
     */
    public static String format(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 自定义格式化时间
     * 
     * @param zonedDateTime
     * @param formatter
     * @return
     */
    public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter formatter) {
        String timeStr = zonedDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 自定义格式化时间
     *
     * @param zonedDateTime
     * @param pattern       自定义格式
     * @return
     */
    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String timeStr = zonedDateTime.format(formatter);
        return timeStr;
    }

    /**
     * 根据当前服务器配置的语言 格式化生成日期<br>
     * 默认为年-月-日<br>
     * {@link Languages#getDateFormatter()}
     * 
     * @param time
     * @return
     */
    public static String formatDate(long time) {
        int language = ServerSettings.getLanguage();
        Languages languages = Languages.valueOf(language);
        if (languages != null) {
            return format(time, languages.getDateFormatter());
        }
        return format(time, DATE_FORMATTER);
    }

    /**
     * 年-月-日 时:分:秒
     * 
     * @return
     */
    private static DateFormat getFullDateFormat() {
        DateFormat df = threadLocal.get();
        if (df == null) {
            df = new SimpleDateFormat(DATETIME_PATTERN);
            threadLocal.set(df);
        }
        return df;
    }
    // ----------------------时间转字符串------------------------------

    // --------------------------字符串转时间----------------------------------------------
    public static Date parseDate(String dateStr) throws ParseException {
        return getFullDateFormat().parse(dateStr);
    }

    public static Date parseDate(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateStr);
    }

    public static Instant parseInstant(String dateStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
        ZonedDateTime dateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return dateTime.toInstant();
    }

    public static Instant parseInstant(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
        ZonedDateTime dateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return dateTime.toInstant();
    }

    public static LocalDate parseLocalDate(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        return localDate;
    }

    public static LocalDateTime parseLocalDateTime(String dateStr) {
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
        return dateTime;
    }

    public static LocalDateTime parseLocalDateTime(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
        return dateTime;
    }

    public static ZonedDateTime parseZonedDateTime(String dateStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
        ZonedDateTime dateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return dateTime;
    }

    public static ZonedDateTime parseZonedDateTime(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
        ZonedDateTime dateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return dateTime;
    }

    public static ZonedDateTime timestamp2DateTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime;
    }

    public static LocalDate timestamp2LocalDate(long time) {
        ZonedDateTime dateTime = timestamp2DateTime(time);
        LocalDate date = dateTime.toLocalDate();
        return date;
    }

    public static long datetime2timestamp(ZonedDateTime dateTime) {
        long timestamp = TimeUnit.SECONDS.toMillis(dateTime.toEpochSecond());
        return timestamp;
    }

    public static long datetime2timestamp(LocalDateTime dateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        long timestamp = TimeUnit.SECONDS.toMillis(zonedDateTime.toEpochSecond());
        return timestamp;
    }

}
