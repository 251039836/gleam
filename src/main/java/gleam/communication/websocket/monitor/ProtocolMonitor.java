package gleam.communication.websocket.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.netty.util.AttributeKey;

/**
 * 协议监控<br>
 * 监控前端发送协议频率和次数
 * 
 * @author hdh
 *
 */
public class ProtocolMonitor {
    /**
     * 客户端发送协议频率监控
     */
    public final static AttributeKey<ProtocolMonitor> ATTR_KEY = AttributeKey.valueOf(ProtocolMonitor.class.getSimpleName());

    /**
     * 客户端发送协议频率监控上限<br>
     * 玩家在指定时间内可发送的协议数量上限
     */
    public final static int PROTOCOL_FREQUENCY_MONITOR_NUM = 30;
    /**
     * 客户端发送协议频率监控测速时间<br>
     * 
     */
    public final static long PROTOCOL_FREQUENCY_MONITOR_TIME = TimeUnit.SECONDS.toMillis(1);

    /**
     * 最近的n次协议发送时间
     */
    private final Queue<Long> receiveTimes = new ArrayBlockingQueue<>(PROTOCOL_FREQUENCY_MONITOR_NUM);

    private final Map<Integer, Long> protocolNums = new ConcurrentHashMap<>();

    public List<Integer> getLargerProtocolIds(int num) {
        List<Entry<Integer, Long>> list = new ArrayList<>(protocolNums.entrySet());
        list.sort((entry1, entry2) -> {
            return Long.compare(entry1.getValue(), entry2.getValue());
        });
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < num && i < list.size(); i++) {
            Entry<Integer, Long> entry = list.get(i);
            result.add(entry.getKey());
        }
        return result;
    }

    public Map<Integer, Long> getProtocolNums() {
        return protocolNums;
    }

    public Queue<Long> getReceiveTimes() {
        return receiveTimes;
    }

    /**
     * 接受协议 判断是否超速
     * 
     * @param protocolId
     * @param now
     * @return
     */
    public boolean receiveProtocol(int protocolId, long now) {
        protocolNums.compute(protocolId, (tmpId, oldValue) -> {
            if (oldValue == null) {
                return 1l;
            }
            return oldValue + 1;
        });
        boolean addSuccess = receiveTimes.offer(now);
        if (!addSuccess) {
            Long firstTime = receiveTimes.poll();
            if (firstTime + PROTOCOL_FREQUENCY_MONITOR_TIME >= now) {
                return true;
            }
            receiveTimes.offer(now);
        }
        return false;
    }

}
