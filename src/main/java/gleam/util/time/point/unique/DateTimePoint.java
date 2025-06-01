package gleam.util.time.point.unique;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 指定具体年月日 时分秒的时间点
 * 
 * @author hdh
 *
 */
public class DateTimePoint implements UniqueTimePoint {

    private final int year;

    private final int month;

    private final int day;

    private final int hour;

    private final int minute;

    private final int second;

    private final long uniqueTime;

    public DateTimePoint(int year, int month, int day, int hour, int minute, int second) {
        super();
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        // 计算该时间点
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime dateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, zoneId);
        uniqueTime = dateTime.toInstant().toEpochMilli();
    }

    @Override
    public long getUniqueTime() {
        return uniqueTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

}
