package gleam.communication.message;

/**
 * @author redback
 * @version 1.00
 * @time 2023-7-12 11:48
 */
public interface RetryableMessageListener<T extends RetryableCapsule> {

    void whenMessageRemove(T m);

    void whenMessageExpire(T m);

    void sendMessage(T t);

}
