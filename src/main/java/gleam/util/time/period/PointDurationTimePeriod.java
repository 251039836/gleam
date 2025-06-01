package gleam.util.time.period;

import gleam.util.time.TimePeriod;
import gleam.util.time.TimePoint;
import gleam.util.tuple.Pair;

/**
 * 由开始时间点+持续时间组成的时间段
 * 
 * @author hdh
 *
 */
public class PointDurationTimePeriod implements TimePeriod {
	/**
	 * 时间段的开始时间点
	 */
	private final TimePoint timePoint;
	/**
	 * 时间段的持续时间
	 */
	private final long duration;

	public PointDurationTimePeriod(TimePoint timePoint, long duration) {
		if (timePoint == null) {
			throw new NullPointerException("timePoint is null");
		}
		if (duration <= 0) {
			throw new NullPointerException("duration <=0");
		}
		this.timePoint = timePoint;
		this.duration = duration;
	}

	@Override
	public Pair<Long, Long> getCurPeriod(long time) {
		long lastStartTime = timePoint.getLastTime(time);
		long lastEndTime = lastStartTime + duration;
		if (lastStartTime <= time && time <= lastEndTime) {
			Pair<Long, Long> result = new Pair<>(lastStartTime, lastEndTime);
			return result;
		}
		return null;
	}

	@Override
	public boolean inThePeriod(long time) {
		long lastStartTime = timePoint.getLastTime(time);
		long lastEndTime = lastStartTime + duration;
		if (lastStartTime <= time && time <= lastEndTime) {
			return true;
		}
		return false;
	}

	public TimePoint getTimePoint() {
		return timePoint;
	}

	public long getDuration() {
		return duration;
	}

}
