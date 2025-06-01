package gleam.communication.message;

/**
 * @author redback
 * @version 1.00
 * @time 2023-7-12 10:26
 */
public interface RetryableCapsule {

    long getId();

    RetryableInfo getMessage();

}
