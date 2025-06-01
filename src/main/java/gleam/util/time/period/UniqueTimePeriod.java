package gleam.util.time.period;

import gleam.util.time.TimePeriod;
import gleam.util.time.TimeUtil;

/**
 * 两个时间戳确定的唯一时间段
 * 
 * @author redback
 * @version 1.00
 * @time 2020-10-12 11:35
 */
public interface UniqueTimePeriod extends TimePeriod {

	/**
	 * 开始时间
	 * 
	 * @return
	 */
	long getStartTime();

	/**
	 * 结束时间
	 * 
	 * @return
	 */
	long getEndTime();

	default int getSecondStartTime() {
		return TimeUtil.ms2s(getStartTime());
	}

	default int getSecondEndTime() {
		return TimeUtil.ms2s(getEndTime());
	}

	@Override
	default boolean inThePeriod(long time) {
		return time >= getStartTime() && getEndTime() >= time;
	}

}
