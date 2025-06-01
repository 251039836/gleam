package gleam.util.time.point.unique;

/**
 * 固定时间点<br>
 * 一个指定的时间戳
 * 
 * @author hdh
 *
 */
public class TimestampTimePoint implements UniqueTimePoint {

    private final long timestamp;

    public TimestampTimePoint(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getUniqueTime() {
        return timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
