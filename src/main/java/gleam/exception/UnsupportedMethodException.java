package gleam.exception;

/**
 * 不支持该方法
 * 
 * @author hdh
 *
 */
public class UnsupportedMethodException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6198015078673573498L;

    public UnsupportedMethodException() {
        super();
    }

    public UnsupportedMethodException(String message) {
        super(message);
    }
}
