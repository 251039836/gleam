package gleam.util.time.point;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import gleam.util.tuple.Pair;

/**
 * 基础时间点<br>
 * 每天的指定时分秒
 * 
 * 
 * @author hdh
 *
 */
public class BasicTimePoint extends AbstractTimePoint {

	protected final int hour;

	protected final int minute;

	protected final int second;

	public BasicTimePoint(int hour, int minute) {
		this(hour, minute, 0);
	}

	public BasicTimePoint(int hour, int minute, int second) {
		super();
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	@Override
	protected Pair<Long, Long> calculateTime(long time) {
//	    LocalTime point = LocalTime.of(hour, minute, second);
//	    LocalDate today=LocalDate.now();
//	    LocalDateTime dateTime = LocalDateTime.of(today, point);
//	    dateTime.toInstant((ZoneOffset) ZoneOffset.systemDefault());

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		long curDayTimePoint = calendar.getTimeInMillis();
		long lastTime = 0;
		long nextTime = 0;
		if (curDayTimePoint <= time) {
			// 已过该时间
			lastTime = curDayTimePoint;
			nextTime = curDayTimePoint + TimeUnit.DAYS.toMillis(1);
		} else {
			// 还没到该时间
			nextTime = curDayTimePoint;
			lastTime = curDayTimePoint - TimeUnit.DAYS.toMillis(1);
		}
		Pair<Long, Long> timePair = new Pair<>(lastTime, nextTime);
		return timePair;
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
