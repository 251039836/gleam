package gleam.util.time.point;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import gleam.util.tuple.Pair;

/**
 * 每个小时指定分钟0秒<br>
 * 
 * @author hdh
 *
 */
public class MinuteTimePoint extends AbstractTimePoint {

	/**
	 * 每个小时指定分钟0秒
	 */
	private final int minute;

	public MinuteTimePoint(int minute) {
		if (minute < 0 || minute >= 60) {
			throw new IllegalArgumentException("MinuteTimePoint illegal minute:" + minute);
		}
		this.minute = minute;
	}

	public int getHour() {
		return minute;
	}

	@Override
	protected Pair<Long, Long> calculateTime(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		// 去除秒
		long lastTime = 0;
		long nextTime = 0;
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.MINUTE, minute);
		// 每个小时该分钟0秒
		long curHourPoint = calendar.getTimeInMillis();
		if (time >= curHourPoint) {
			// 已过该时间
			lastTime = curHourPoint;
			nextTime = curHourPoint + TimeUnit.HOURS.toMillis(1);
		} else {
			// 还没到该时间
			nextTime = curHourPoint;
			lastTime = curHourPoint - TimeUnit.HOURS.toMillis(1);
		}
		Pair<Long, Long> timePair = new Pair<>(lastTime, nextTime);
		return timePair;
	}

}
