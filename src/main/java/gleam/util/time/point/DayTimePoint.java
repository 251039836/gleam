package gleam.util.time.point;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gleam.util.tuple.Pair;

/**
 * 满足条件的一天的时间点
 * 
 * @author hdh
 *
 */
public class DayTimePoint extends BasicTimePoint {

    /**
     * 查找天数上限 超出上限不再查找
     */
    protected final static int SELECT_LIMIT = 1000;

    /**
     * 参数类型,参数值<br>
     * {@link Calendar}
     */
    protected final Map<Integer, Integer> fieldValues = new HashMap<>();

    /**
     * 
     * @param fieldValues 若其中有时分秒 会被后面的hour minute second覆盖
     * @param hour
     * @param minute
     * @param second
     */
    public DayTimePoint(Map<Integer, Integer> fieldValues, int hour, int minute, int second) {
        super(hour, minute, second);
        if (fieldValues == null || fieldValues.isEmpty()) {
            throw new IllegalArgumentException("fieldValues is empty.");
        }
        this.fieldValues.putAll(fieldValues);
    }

    @Override
    protected Pair<Long, Long> calculateTime(long time) {
        // 获取上个时间点
        long lastTime = calculateTime(time, false);
        // 获取下个时间点
        long nextTime = calculateTime(time, true);
        return new Pair<>(lastTime, nextTime);
    }

    protected long calculateTime(final long originTime, final boolean future) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(originTime);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        long tmpTime = calendar.getTimeInMillis();
        int changeDay = -1;
        if (future) {
            changeDay = 1;
            if (tmpTime <= originTime) {
                calendar.add(Calendar.DAY_OF_YEAR, changeDay);
            }
        } else {
            changeDay = -1;
            if (tmpTime > originTime) {
                calendar.add(Calendar.DAY_OF_YEAR, changeDay);
            }
        }
        int i = 0;
        while (i < SELECT_LIMIT) {
            tmpTime = calendar.getTimeInMillis();
            if (isMeetDay(calendar)) {
                return tmpTime;
            }
            calendar.add(Calendar.DAY_OF_YEAR, changeDay);
            i++;
        }
        return -1;
    }

    /**
     * 该日期是否满足条件
     * 
     * @param calendar
     * @return
     */
    protected boolean isMeetDay(Calendar calendar) {
        for (Entry<Integer, Integer> entry : fieldValues.entrySet()) {
            int fieldType = entry.getKey();
            int fieldValue = entry.getValue();
            int curValue = calendar.get(fieldType);
            if (curValue != fieldValue) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean needCalculateTime(long time) {
        if (_lastTime >= 0 && _lastTime > time) {
            return true;
        }
        if (_nextTime >= 0 && _nextTime <= time) {
            return true;
        }
        return false;
    }

    public Map<Integer, Integer> getFieldValues() {
        return fieldValues;
    }

}
