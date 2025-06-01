package gleam.exception;

/**
 * 重复注册错误
 * 
 * @author hdh
 * @time 2022年8月4日
 *
 */
public class RepeatRegisterException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5782731539077485527L;

    public RepeatRegisterException(String message) {
        super(message);
    }
}
