package gleam.communication.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author redback
 * @version 1.00
 * @time 2023-7-12 10:28
 */

public class RetryableBox<T extends RetryableCapsule> implements RetryableTrigger{

    private Map<Long, T> messageMap = new ConcurrentHashMap<>();

    private RetryableMessageListener<T> listener;


    @Override
    public void check(long time) {
        if (messageMap.isEmpty()) {
            return;
        }
        List<Long> expireMsgList = new ArrayList<>();
        for (Map.Entry<Long, T> entry : messageMap.entrySet()) {
            final long id = entry.getKey();
            final T message = entry.getValue();
            RetryableInfo retryableInfo = message.getMessage();
            if (retryableInfo.isExpire()) {
                expireMsgList.add(id);
                continue;
            }
            int retryNum = retryableInfo.getRetryNum();
            long lastSendTime = retryableInfo.getLastRetryTime();
            long nextInterval = retryableInfo.getNextRetryTime(retryNum);
            if (lastSendTime + nextInterval > time) {
                continue;
            }
            sendMessage(message);
            retryableInfo.updateRetryInfo(time);
        }
        for (Long id : expireMsgList) {
            T m = messageMap.remove(id);
            if (m != null && listener != null) {
                listener.whenMessageExpire(m);
            }
        }
    }

    public T removeMessage(long id){
        T remove = messageMap.remove(id);
        if (remove != null && listener != null) {
            listener.whenMessageRemove(remove);
        }
        return remove;
    }

    public T getMessage(long id){
        return messageMap.get(id);
    }

    public T addMessageIfAbsent(long id, Supplier<T> supplier){
        T message = messageMap.putIfAbsent(id, supplier.get());
        if (message == null) {
            message = messageMap.get(id);
            sendMessage(message);
        }
        return message;
    }

    protected void sendMessage(T m){
        if (listener != null) {
            listener.sendMessage(m);
        }
    }


    public void setListener(RetryableMessageListener<T> listener) {
        this.listener = listener;
    }
}
