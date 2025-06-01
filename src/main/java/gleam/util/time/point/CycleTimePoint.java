package gleam.util.time.point;

import java.util.concurrent.TimeUnit;

import gleam.util.time.point.unique.UniqueTimePoint;
import gleam.util.tuple.Pair;

/**
 * 循环时间点
 * 
 * @author hdh
 *
 */
public class CycleTimePoint extends AbstractTimePoint {
    /**
     * 首个时间点
     */
    private final UniqueTimePoint firstPoint;
    /**
     * 循环间隔<br>
     * 天<br>
     * 1为从首个时间点开始后 每天
     */
    private final int cycleIntervalDay;
    /**
     * 循环次数<br>
     * <=0则为无限次
     */
    private final int cycleTimes;

    /**
     * 上次计算时 使用的首个时间点
     */
    private long oldFirstTime;

    /**
     * 停止循环时间
     */
    private long stopCycleTime;

    public CycleTimePoint(UniqueTimePoint firstPoint, int cycleIntervalDay, int cycleTimes) {
        if (firstPoint == null) {
            throw new NullPointerException("firstPoint is null.");
        }
        if (cycleIntervalDay <= 0) {
            throw new IllegalArgumentException("cycleIntervalDay must be greater than zero.");
        }
        this.firstPoint = firstPoint;
        this.cycleIntervalDay = cycleIntervalDay;
        this.cycleTimes = cycleTimes;
    }

    @Override
    protected Pair<Long, Long> calculateTime(long time) {
        long firstTime = firstPoint.getLastTime(time);
        if (firstTime != oldFirstTime) {// 重新计算不再循环时间
            this.oldFirstTime = firstTime;
            if (cycleTimes > 0) {
                long intervalTime = TimeUnit.DAYS.toMillis(cycleIntervalDay);
                this.stopCycleTime = this.oldFirstTime + intervalTime * (cycleTimes - 1);
            } else {
                this.stopCycleTime = Long.MAX_VALUE;
            }
        }
        long lastTime = firstTime;
        long nextTime = lastTime;
        if (time >= firstTime && time < stopCycleTime) {
            // 已过了首次的时间点
            long cycleTime = TimeUnit.DAYS.toMillis(cycleIntervalDay);
            long throughTime = time - firstTime;
            long count = throughTime / cycleTime;
            lastTime = firstTime + cycleTime * count;
            nextTime = lastTime + cycleTime;
        } else if (time >= stopCycleTime) {
            lastTime = stopCycleTime;
            nextTime = stopCycleTime;
        }
        return Pair.of(lastTime, nextTime);
    }


    public int getCycleIntervalDay() {
        return cycleIntervalDay;
    }

    public int getCycleTimes() {
        return cycleTimes;
    }

    public void setStopCycleTime(long stopCycleTime) {
        this.stopCycleTime = stopCycleTime;
    }

    public UniqueTimePoint getFirstPoint() {
        return firstPoint;
    }

    public long getOldFirstTime() {
        return oldFirstTime;
    }

    public long getStopCycleTime() {
        return stopCycleTime;
    }

    @Override
    protected boolean needCalculateTime(long time) {
        if (_lastTime == 0 && _nextTime == 0) {
            // 未计算过
            return true;
        }
        long firstTime = firstPoint.getLastTime(time);
        if (firstTime != oldFirstTime) {
            // 开始时间点发生了变化
            return true;
        }
        if (time < oldFirstTime) {
            // 比开始时间点早
            return false;
        }
        // 过了不再循环时间
        if (_lastTime == stopCycleTime) {
            return false;
        }
        // 比开始时间点晚
        if (_lastTime > time || _nextTime <= time) {
            return true;
        }
        return false;
    }

    public void setOldFirstTime(long oldFirstTime) {
        this.oldFirstTime = oldFirstTime;
    }
}
