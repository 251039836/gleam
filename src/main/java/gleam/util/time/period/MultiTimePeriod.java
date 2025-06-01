package gleam.util.time.period;

import java.util.ArrayList;
import java.util.List;

import gleam.util.time.TimePeriod;
import gleam.util.tuple.Pair;

/**
 * 多个时间段
 * 
 * @author hdh
 *
 */
public class MultiTimePeriod implements TimePeriod {

	protected List<TimePeriod> periods = new ArrayList<>();

	public void addPeriod(TimePeriod period) {
		if (period instanceof MultiTimePeriod) {
			MultiTimePeriod multiTimePeriod = (MultiTimePeriod) period;
			List<TimePeriod> otherPeriods = multiTimePeriod.getPeriods();
			for (TimePeriod otherPeriod : otherPeriods) {
				addPeriod(otherPeriod);
			}
			return;
		}
		periods.add(period);
	}

	@Override
	public Pair<Long, Long> getCurPeriod(long time) {
		for (TimePeriod period : periods) {
			Pair<Long, Long> curPeriod = period.getCurPeriod(time);
			if (curPeriod != null) {
				return curPeriod;
			}
		}
		return null;
	}

	public List<TimePeriod> getPeriods() {
		return periods;
	}

	@Override
	public boolean inThePeriod(long time) {
		for (TimePeriod period : periods) {
			if (period.inThePeriod(time)) {
				return true;
			}
		}
		return false;
	}

	public void setPeriods(List<TimePeriod> periods) {
		this.periods = periods;
	}

}
