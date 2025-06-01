package gleam.communication.message;

/**
 * @author redback
 * @version 1.00
 * @time 2023-7-12 11:37
 */
public interface RetryableTrigger {

    void check(long time);

}
