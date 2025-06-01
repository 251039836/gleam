package gleam.util.time.point;

import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.util.time.TimePoint;
import gleam.util.tuple.Pair;

public abstract class AbstractTimePoint implements TimePoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected long _lastTime;

    protected long _nextTime;
    /**
     * 可升级的读写锁
     */
    protected StampedLock lock = new StampedLock();

    @Override
    public long getLastTime(long time) {
        long stamp = lock.readLock();
        try {
            stamp = checkAndRefresh(time, stamp);
            return _lastTime;
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public long getNextTime(long time) {
        long stamp = lock.readLock();
        try {
            stamp = checkAndRefresh(time, stamp);
            return _nextTime;
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public boolean isAcross(long time1, long time2) {
        long lastTime = getLastTime(time2);
        if (lastTime > time1 && lastTime <= time2) {
            return true;
        }
        return false;
    }

    /**
     * 检查并尝试刷新时间
     * 
     * @param time
     * @param stamp 当前读锁标记
     * @return stamp 当前使用的锁标记(若升级为写锁 则为写锁标记)
     */
    protected long checkAndRefresh(long time, long stamp) {
        if (!needCalculateTime(time)) {
            return stamp;
        }
        // 尝试读锁转写锁
        long writeLock = lock.tryConvertToWriteLock(stamp);
        if (writeLock == 0) {
            // 转化失败 释放原读锁 获取写锁
            lock.unlockRead(stamp);
            writeLock = lock.writeLock();
        }
        try {
            Pair<Long, Long> timePair = calculateTime(time);
            _lastTime = timePair.getLeft();
            _nextTime = timePair.getRight();
        } catch (Exception e) {
            logger.error("calculateTime error.", e);
        }
        // 当前的锁必须还回去
        return writeLock;
    }

    /**
     * 是否需要重新计算时间
     * 
     * @param time
     * @return
     */
    protected boolean needCalculateTime(long time) {
        if (_lastTime > time || _nextTime <= time) {
            return true;
        }
        return false;
    }

    /**
     * 计算指定时间之前和之后的时间点的时间
     * 
     * @param time
     * @return [left]lastTime,[right]nextTime
     */
    protected abstract Pair<Long, Long> calculateTime(long time);
}
