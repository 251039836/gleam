package gleam.util.http;

/**
 * @author redback
 * @version 1.00
 * @time 2021-7-15 17:39
 */
public interface HttpStringResponseCallback {

    void failed(Exception e);

    void success(String content);

}
