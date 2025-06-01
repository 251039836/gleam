package gleam.util.time.period;

import gleam.util.time.TimePeriod;
import gleam.util.time.TimePoint;
import gleam.util.tuple.Pair;

/**
 * 时间点到时间点组成的时间段
 * 
 * @author hdh
 *
 */
public class Point2PointTimePeriod implements TimePeriod {
	/**
	 * 开始时间点
	 */
	private final TimePoint beginPoint;
	/**
	 * 结束时间点
	 */
	private final TimePoint endPoint;

	public Point2PointTimePeriod(TimePoint beginPoint, TimePoint endPoint) {
		if (beginPoint == null) {
			throw new NullPointerException("beginPoint is null");
		}
		if (endPoint == null) {
			throw new NullPointerException("endPoint is null");
		}
		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
	}

	@Override
	public Pair<Long, Long> getCurPeriod(long time) {
		long lastStartTime = beginPoint.getLastTime(time);
		long lastEndTime = endPoint.getNextTime(lastStartTime);
		if (lastStartTime <= time && lastEndTime >= time) {
			Pair<Long, Long> result = new Pair<>(lastStartTime, lastEndTime);
			return result;
		}
		return null;
	}

	@Override
	public boolean inThePeriod(long time) {
		long lastStartTime = beginPoint.getLastTime(time);
		long lastEndTime = endPoint.getNextTime(lastStartTime);
		if (lastStartTime <= time && lastEndTime >= time) {
			return true;
		}
		return false;
	}

	public TimePoint getBeginPoint() {
		return beginPoint;
	}

	public TimePoint getEndPoint() {
		return endPoint;
	}

}
