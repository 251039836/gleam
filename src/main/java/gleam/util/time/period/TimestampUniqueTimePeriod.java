package gleam.util.time.period;

import gleam.util.time.TimeUtil;
import gleam.util.tuple.Pair;

/**
 * 两个 unix 时间戳指定的时间段
 * 
 * @author redback
 * @version 1.00
 * @time 2020-9-18 10:12
 */
public class TimestampUniqueTimePeriod implements UniqueTimePeriod {

	private static final TimestampUniqueTimePeriod empty = new TimestampUniqueTimePeriod(0, 0);

	public static UniqueTimePeriod emptyTimeInterval() {
		return empty;
	}

	/**
	 * 通过开始时间和结束时间构造一个事件端
	 * 
	 * @param startTime 开始时间
	 * @param endTime   结束时间
	 * @return TimeInterval
	 */
	public static TimestampUniqueTimePeriod valueOf(long startTime, long endTime) {
		if (startTime > endTime) {
			throw new IllegalArgumentException(String.format("开始时间【%s】比结束时间 【%s】大", startTime, endTime));
		}
		return new TimestampUniqueTimePeriod(startTime, endTime);
	}

	/**
	 * 通过开始时间和持续时间构造一个时间段
	 * 
	 * @param startTime 开始时间
	 * @param duration  持续时间 s
	 * @return TimeInterval
	 */
	public static TimestampUniqueTimePeriod valueOf(long startTime, int duration) {
		return valueOf(startTime, startTime + duration * TimeUtil.SECOND_MILLISECONDS);
	}

	/**
	 * 从现在开始+持续时间构造一个事件端
	 * 
	 * @param duration 持续时间
	 * @return 持续时间
	 */
	public static TimestampUniqueTimePeriod valueOfNow(int duration) {
		long now = TimeUtil.now();
		return valueOf(now, now + duration);
	}

	/**
	 * 开始时间
	 */
	private final long startTime;

	/**
	 * 结束时间
	 */
	private final long endTime;

	private TimestampUniqueTimePeriod(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public Pair<Long, Long> getCurPeriod(long time) {
		if (inThePeriod(time)) {
			Pair<Long, Long> result = new Pair<>(startTime, endTime);
			return result;
		}
		return null;
	}

}
