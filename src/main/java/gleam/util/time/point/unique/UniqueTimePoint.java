package gleam.util.time.point.unique;

import gleam.util.time.TimePoint;

/**
 * 唯一时间点<br>
 * 该时间点只对应1个时间戳
 * 
 * @author hdh
 *
 */
public interface UniqueTimePoint extends TimePoint {

    /**
     * 该时间点对应的唯一的时间戳
     * 
     * @return
     */
    long getUniqueTime();

    @Override
    default long getLastTime(long time) {
        return getUniqueTime();
    }

    @Override
    default long getNextTime(long time) {
        return getUniqueTime();
    }

    @Override
    default boolean isAcross(long time1, long time2) {
        long unique = getUniqueTime();
        if (unique > time1 && unique <= time2) {
            return true;
        }
        return false;
    }

}
