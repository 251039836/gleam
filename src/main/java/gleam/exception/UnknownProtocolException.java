package gleam.exception;

import java.io.IOException;

/**
 * 未知协议
 * 
 * @author hdh
 *
 */
public class UnknownProtocolException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -3401411088827107682L;

    public UnknownProtocolException() {
        super();
    }

    public UnknownProtocolException(String message) {
        super(message);
    }

}
