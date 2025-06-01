package gleam.exception;

/**
 * 不是唯一配置
 * 
 * @author hdh
 *
 */
public class NotUniqueConfigException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2699468488320306768L;

    public NotUniqueConfigException(String message) {
        super(message);
    }

}
