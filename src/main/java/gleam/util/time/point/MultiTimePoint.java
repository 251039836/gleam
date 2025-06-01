package gleam.util.time.point;

import java.util.ArrayList;
import java.util.List;

import gleam.util.time.TimePoint;

/**
 * 多个时间点
 * 
 * @author hdh
 *
 */
public class MultiTimePoint implements TimePoint {

	private List<TimePoint> points = new ArrayList<>();

	public void addPoint(TimePoint point) {
		if (point instanceof MultiTimePoint) {
			MultiTimePoint multiTimePoint = (MultiTimePoint) point;
			List<TimePoint> otherPoints = multiTimePoint.getPoints();
			for (TimePoint otherPoint : otherPoints) {
				addPoint(otherPoint);
			}
			return;
		}
		points.add(point);
	}

	@Override
	public long getLastTime(long time) {
		long lastTime = 0;
		for (TimePoint point : points) {
			long tmpLastTime = point.getLastTime(time);
			if (tmpLastTime == time) {
				return time;
			}
			if (tmpLastTime > lastTime) {
				lastTime = tmpLastTime;
			}
		}
		return lastTime;
	}

	@Override
	public long getNextTime(long time) {
		long nextTime = Long.MAX_VALUE;
		for (TimePoint point : points) {
			long tmpNextTime = point.getNextTime(time);
			if (tmpNextTime < nextTime) {
				nextTime = tmpNextTime;
			}
		}
		return nextTime;
	}

	public List<TimePoint> getPoints() {
		return points;
	}

	@Override
	public boolean isAcross(long time1, long time2) {
		for (TimePoint point : points) {
			if (point.isAcross(time1, time2)) {
				return true;
			}
		}
		return false;
	}
}
