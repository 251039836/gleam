package gleam.util.time.point;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import gleam.util.tuple.Pair;

/**
 * 每天指定的小时整点<br>
 * 
 * @author hdh
 *
 */
public class HourTimePoint extends AbstractTimePoint {

	/**
	 * 指定小时整点<br>
	 */
	private final int hour;

	public HourTimePoint(int hour) {
		this.hour = hour;
	}

	public int getHour() {
		return hour;
	}

	@Override
	protected Pair<Long, Long> calculateTime(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		// 去除分秒
		long lastTime = 0;
		long nextTime = 0;
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		// 当天该小时0点0分
		long curDayTimePoint = calendar.getTimeInMillis();
		if (time >= curDayTimePoint) {
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

}
