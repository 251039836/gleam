package gleam.util.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 * 
 * @author hdh
 *
 */
public class TimeUtil {

    public final static long SECOND_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);
    public final static long MINUTE_MILLISECONDS = TimeUnit.MINUTES.toMillis(1);
    public final static long HALF_HOUR_MILLISECONDS = TimeUnit.MINUTES.toMillis(30);
    public final static long HOUR_MILLISECONDS = TimeUnit.HOURS.toMillis(1);
    public final static long DAY_MILLISECONDS = TimeUnit.DAYS.toMillis(1);
    public final static long WEEK_MILLISECONDS = TimeUnit.DAYS.toMillis(7);
    /**
     * 30天的毫秒数<br>
     */
    public final static long MONTH_MILLISECONDS = TimeUnit.DAYS.toMillis(30);
    public final static long YEAR_MILLISECONDS = TimeUnit.DAYS.toMillis(365);

    private final static LocalTime ZERO_TIME = LocalTime.of(0, 0);
    /**
     * 1天开始的时间点<br>
     * 用于判断是否同1天<br>
     * 跨天重置 不一定是0点 而以该时间点为准<br>
     * FIXME 目前主策结论是 0点跨天 不是0点跨天的 让那个功能相关的策划找他 20200817
     */
    public final static LocalTime DAY_BEGIN_TIME = LocalTime.of(0, 0);
    /**
     * 充值（月卡，年卡）等计算，一天开始的时间
     */
    public final static LocalTime PAY_BEGIN_TIME = LocalTime.of(0, 0);
    /**
     * 一周的开始时间<br>
     * 周1
     */
    public final static WeekFields WEEK_BEGIN_DATE = WeekFields.ISO;

    /**
     * 时间偏移量<br>
     * 为了给测试方便测试
     */
    private static volatile long OFFSET_TIME = 0;

    public static long getOffsetTime() {
        return OFFSET_TIME;
    }

    public static void setOffsetTime(long offsetTime) {
        OFFSET_TIME = offsetTime;
    }

    /**
     * 经过的周数<br>
     * 每跨过1次周的起始时间点 视为1次<br>
     * 同1周内返回0
     * 
     * @param beginTime 毫秒
     * @param endTime   毫秒
     * @return
     */
    public static int getAcrossWeek(long beginTime, long endTime) {
        long diff = endTime - beginTime;
        long week = diff / WEEK_MILLISECONDS;
        long remain = diff % WEEK_MILLISECONDS;
        long endTimeMondayBeginTime = getWeekBeginTime(endTime);
        if (endTimeMondayBeginTime > endTime - remain) {
            week += 1;
        }
        return (int) (week + 1);
    }

    /**
     * 获取这天的开始时间点<br>
     * 不使用自然天 而是使用自定义的某个时间点作为1天的分割点<br>
     * {@link TimeUtil#DAY_BEGIN_TIME}
     * 
     * @param time 毫秒
     * @return 毫秒
     */
    public static long getDayBeginTime(long time) {
        ZonedDateTime dateTime = DateFormatUtils.timestamp2DateTime(time);
        LocalDate date = dateTime.toLocalDate();
        ZonedDateTime beginDateTime = ZonedDateTime.of(date, DAY_BEGIN_TIME, ZoneId.systemDefault());
        long beginTimestamp = TimeUnit.SECONDS.toMillis(beginDateTime.toEpochSecond());
        if (time < beginTimestamp) {
            beginTimestamp -= DAY_MILLISECONDS;
        }
        return beginTimestamp;
    }

    /**
     * 今天是本周的第几天
     * 
     * @return 1星期一~7星期日
     */
    public static int getDayOfWeek() {
        ZonedDateTime now = nowDateTime();
        return now.getDayOfWeek().getValue();
    }

    /**
     * 获取该时间是一周的第几天
     * 
     * @param now
     * @return 1星期一~7星期日
     */
    public static int getDayOfWeek(long now) {
        ZonedDateTime dateTime = DateFormatUtils.timestamp2DateTime(now);
        return dateTime.getDayOfWeek().getValue();
    }

    /**
     * 获取两天时间戳的相差天数<br>
     * 跨天 即使只有1秒 也视为1天<br>
     * 
     * @param startTime 起始时间（毫秒）
     * @param endTime   结束时间（毫秒）
     * @return 天数 >=0
     */
    public static int getDifferDay(long startTime, long endTime) {
        if (endTime <= startTime) {
            return 0;
        }
        long diffTime = endTime - startTime;
        int day = (int) (diffTime / DAY_MILLISECONDS);
        long remainMs = diffTime % DAY_MILLISECONDS;
        if (remainMs > 0) {
            // 结束时间的当天起始时间
            long endTimeDayBeginTime = getDayBeginTime(endTime);
            if (endTimeDayBeginTime > endTime - remainMs) {
                day += 1;
            }
        }
        return day;
    }

    /**
     * 获取2个日期相差的天数<br>
     * 若为同一天 返回0<br>
     * 若开始日期比结束日期晚 返回0
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDifferDay(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return 0;
        }
        long diff = startDate.until(endDate, ChronoUnit.DAYS);
        return (int) diff;
    }

    /**
     * 获取2个时间相差的天数<br>
     * 若为同一天 返回0<br>
     * 若开始日期比结束日期晚 返回0
     * 
     * @param startDate
     * @param endTime
     * @return
     */
    public static int getDifferDay(LocalDate startDate, long endTime) {
        LocalDate endDate = DateFormatUtils.timestamp2LocalDate(endTime);
        if (startDate.isAfter(endDate)) {
            return 0;
        }
        long diff = startDate.until(endDate, ChronoUnit.DAYS);
        return (int) diff;
    }

    /**
     * 获取2个日期相差月份数<br>
     * 按自然月计算<br>
     * 若在同1个月内 返回0<br>
     * 若开始日期比结束日期晚 返回0
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDifferMonth(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return 0;
        }
        int startYear = startDate.getYear();
        int startMonth = startDate.getMonthValue();// 1~12
        int endYear = endDate.getYear();
        int endMonth = endDate.getMonthValue();
        int diff = (endYear - startYear) * 12 + endMonth - startMonth;
        return diff;
    }

    /**
     * 获取今天的 0 点
     *
     * @return
     */
    public static long getTodayBeginTime() {
        long now = TimeUtil.now();
        return getDayBeginTime(now);
    }

    /**
     * 获取下一天的 0 点
     * 
     * @return
     */
    public static long getNextDayBeginTime() {
        long now = TimeUtil.now();
        return getDayBeginTime(now) + DAY_MILLISECONDS;
    }

    /**
     * 获取指定时间下一天的 0 点
     * 
     * @param time
     * @return
     */
    public static long getNextDayBeginTime(long time) {
        return getDayBeginTime(time) + DAY_MILLISECONDS;
    }

    /**
     * 获取下个月第一天的0点开始时间点<br>
     *
     * @param time 毫秒
     * @return 毫秒
     */
    public static long getNextMonthBeginTime(long time) {
        ZonedDateTime dateTime = DateFormatUtils.timestamp2DateTime(time);
        LocalDate date = dateTime.toLocalDate();
        int month = date.getMonthValue();
        int year = date.getYear();
        if (month + 1 >= 13) {
            year += 1;
            month = 1;
        } else {
            month += 1;
        }
        LocalDate localDate = LocalDate.of(year, month, 1);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());

        return TimeUnit.SECONDS.toMillis(zonedDateTime.toEpochSecond());
    }

    /**
     * 获取指定时间下一周的 0 点
     * 
     * @param time
     * @return
     */
    public static long getNextWeekBeginTime(long time) {
        return getWeekBeginTime(time) + WEEK_MILLISECONDS;
    }

    /**
     * 获取现在到某个时间相差的天数 不支持过去的时间
     * 
     * @param time 目标时间时间戳（毫秒）， time > now
     * @return 天数 >=0
     */
    public static int getNowDifferDay(long time) {
        return getDifferDay(now(), time);
    }

    /**
     * 获取充值时间计算，这天的开始时间点<br>
     * 不使用自然天 而是使用自定义的某个时间点作为1天的分割点<br>
     * {@link TimeUtil#PAY_BEGIN_TIME}
     *
     * @param time 毫秒
     * @return 毫秒
     */
    public static long getPayDayBeginTime(long time) {
        ZonedDateTime dateTime = DateFormatUtils.timestamp2DateTime(time);
        LocalDate date = dateTime.toLocalDate();
        ZonedDateTime beginDateTime = ZonedDateTime.of(date, PAY_BEGIN_TIME, ZoneId.systemDefault());
        long beginTimestamp = TimeUnit.SECONDS.toMillis(beginDateTime.toEpochSecond());
        if (time < beginTimestamp) {
            beginTimestamp -= DAY_MILLISECONDS;
        }
        return beginTimestamp;
    }

    /**
     * 获取该日期的时间戳<br>
     * 该日期0点0分0秒
     * 
     * @param date
     * @return
     */
    public static long getTimestamp(LocalDate date) {
        ZonedDateTime zoneDateTime = ZonedDateTime.of(date, ZERO_TIME, ZoneId.systemDefault());
        long timestamp = TimeUnit.SECONDS.toMillis(zoneDateTime.toEpochSecond());
        return timestamp;
    }

    /**
     * 获取该时间的时间戳<br>
     * 
     * @param date
     * @param time
     * @return
     */
    public static long getTimestamp(LocalDate date, LocalTime time) {
        ZonedDateTime zoneDateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault());
        long timestamp = TimeUnit.SECONDS.toMillis(zoneDateTime.toEpochSecond());
        return timestamp;
    }

    /**
     * 获取该时间的时间戳<br>
     * 
     * @param dateTime
     * @return
     */
    public static long getTimestamp(LocalDateTime dateTime) {
        ZonedDateTime zoneDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        long timestamp = TimeUnit.SECONDS.toMillis(zoneDateTime.toEpochSecond());
        return timestamp;
    }

    /**
     * 获取该时间的时间戳<br>
     * 
     * @param dateTime
     * @return
     */
    public static long getTimestamp(ZonedDateTime zoneDateTime) {
        long timestamp = TimeUnit.SECONDS.toMillis(zoneDateTime.toEpochSecond());
        return timestamp;
    }

    /**
     * 获取当前小时
     * 
     * @return [0,23]
     */
    public static int getTodayHour() {
        ZonedDateTime now = nowDateTime();
        return now.getHour();
    }

    /**
     * 获取本周第一天的0点开始时间点<br>
     * 目前周一作为一周第一天<br>
     * TODO 这里要确定清楚, 欧美地区以周天作为第一天, 中国以周一作为每周第一天. 不同地区的重置时间是否都设置为周一 {@link TimeUtil#DAY_BEGIN_TIME}
     * 
     * @param time 毫秒
     * @return 毫秒
     */
    public static long getWeekBeginTime(long time) {
        ZonedDateTime dateTime = DateFormatUtils.timestamp2DateTime(time);
        LocalDate date = dateTime.toLocalDate();

        TemporalField dayOfWeekTemporalField = WEEK_BEGIN_DATE.dayOfWeek();// 以周一作为每周第一天

        // t.with(dayOfWeekTemporalField, x) 表示获取第几天.
        TemporalAdjuster temporalAdjuster = t -> t.with(dayOfWeekTemporalField, 1);

        LocalDate mondayDate = date.with(temporalAdjuster);

        ZonedDateTime beginDateTime = ZonedDateTime.of(mondayDate, DAY_BEGIN_TIME, ZoneId.systemDefault());
        long beginTimestamp = TimeUnit.SECONDS.toMillis(beginDateTime.toEpochSecond());
        if (time < beginTimestamp) {
            beginTimestamp -= DAY_MILLISECONDS;
        }
        return beginTimestamp;
    }

    /**
     * 该时间是否和当前是同1天<br>
     * 该判断不使用自然天 而是使用自定义的某个时间点作为1天的分割点<br>
     * {@link TimeUtil#DAY_BEGIN_TIME}
     * 
     * @param time
     * @return
     */
    public static boolean isSameDay(long time) {
        long now = now();
        return isSameDay(time, now);
    }

    /**
     * 是否同1天<br>
     * 该判断不使用自然天 而是使用自定义的某个时间点作为1天的分割点<br>
     * {@link TimeUtil#DAY_BEGIN_TIME}
     * 
     * @param time1 毫秒
     * @param time2 毫秒
     * @return
     */
    public static boolean isSameDay(long time1, long time2) {
        long diff = Math.abs(time1 - time2);
        if (diff >= DAY_MILLISECONDS) {
            return false;
        }
        // 获取其中1个时间的所在天的开始时间点
        long dayBeginTime = getDayBeginTime(time1);
        if (time2 < dayBeginTime || time2 >= dayBeginTime + DAY_MILLISECONDS) {
            return false;
        }
        return true;
    }

    /**
     * @param time1 毫秒
     * @param time2 毫秒
     * @return 是否为同一个月
     */
    public static boolean isSameMonth(long time1, long time2) {
        ZonedDateTime dateTime1 = DateFormatUtils.timestamp2DateTime(time1);
        ZonedDateTime dateTime2 = DateFormatUtils.timestamp2DateTime(time2);
        if (dateTime1.getYear() != dateTime2.getYear()) {
            return false;
        }
        return dateTime1.getMonthValue() == dateTime2.getMonthValue();
    }

    /**
     * 是否同1周<br>
     * 该判断不使用自然天 而是使用自定义的某个时间点作为1天的分割点<br>
     * {@link TimeUtil#DAY_BEGIN_TIME}
     * 
     * @param time1 毫秒
     * @param time2 毫秒
     * @return
     */
    public static boolean isSameWeek(long time1, long time2) {
        long diff = Math.abs(time1 - time2);
        if (diff >= WEEK_MILLISECONDS) {
            return false;
        }
        // 获取其中1个时间的所在天的开始时间点
        long mondayBeginTime = getWeekBeginTime(time1);
        if (time2 < mondayBeginTime || time2 >= mondayBeginTime + WEEK_MILLISECONDS) {
            return false;
        }
        return true;
    }

    /**
     * 毫秒转为秒
     * 
     * @param milliseconds
     * @return
     */
    public static int milliseconds2Seconds(long milliseconds) {
        if (milliseconds == 0) {
            return 0;
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return (int) seconds;
    }

    /**
     * 毫秒转为秒
     * 
     * @param milliseconds
     * @return
     */
    public static int ms2s(long milliseconds) {
        if (milliseconds == 0) {
            return 0;
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return (int) seconds;
    }

    /**
     * 秒转为豪秒
     * 
     * @param milliseconds
     * @return
     */
    public static long s2ms(long seconds) {
        if (seconds == 0) {
            return 0;
        }
        long milliseconds = TimeUnit.SECONDS.toMillis(seconds);
        return milliseconds;
    }

    /**
     * 获取当前时间<br>
     * 该方式有偏移量 方便测试调时间<br>
     * 玩法功能代码 获取当前时间 使用该方法<br>
     * 
     * @return 毫秒
     */
    public static long now() {
        long now = System.currentTimeMillis();
        return now + OFFSET_TIME;
    }

    /**
     * 获取当前时间<br>
     * 
     * @return 带有时区的时间
     */
    public static ZonedDateTime nowDateTime() {
        ZonedDateTime now = ZonedDateTime.now();
        if (OFFSET_TIME != 0) {
            now = now.plus(OFFSET_TIME, ChronoUnit.MILLIS);
        }
        return now;
    }

    /**
     * 获取当前时间<br>
     * 
     * @return 秒
     */
    public static int nowSecond() {
        return milliseconds2Seconds(now());
    }

    /**
     * 获取当前纳秒时间戳
     * 
     * @return
     */
    public static long nowNano() {
        long now = System.currentTimeMillis() + OFFSET_TIME;
        long nanoTime = now * 1000000l + System.nanoTime() % 1000000l;
        return nanoTime;
    }

    /**
     * 获取时间差的时间戳
     * 
     * @return
     */
    public static long getDifferTime(long time) {
        long nowTime = now();
        return Math.abs(nowTime - time);
    }
}
