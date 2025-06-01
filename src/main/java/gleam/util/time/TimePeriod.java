package gleam.util.time;

import gleam.util.tuple.Pair;

/**
 * 时间段<br>
 * 
 * @author hdh
 *
 */
//@JsonDeserialize(using = TimePeriodParser.class)
public interface TimePeriod {

	/**
	 * 若在时间段内 则返回当前时间段的开始结束时间<br>
	 * 若不在 则返回null
	 * 
	 * @param time
	 * @return
	 */
	Pair<Long, Long> getCurPeriod(long time);

	/**
	 * 是否在时间段内<br>
	 * [t1,t2]
	 * 
	 * @param time
	 * @return
	 */
	boolean inThePeriod(long time);

}
