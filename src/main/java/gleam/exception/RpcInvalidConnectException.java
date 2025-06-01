package gleam.exception;

/**
 * rpc链接失效
 * 
 * @author hdh
 *
 */
public class RpcInvalidConnectException extends RpcException {

    /**
     * 
     */
    private static final long serialVersionUID = -4238489289360297371L;

    public RpcInvalidConnectException() {
    }

    public RpcInvalidConnectException(String msg) {
        super(msg);
    }

    public RpcInvalidConnectException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcInvalidConnectException(Throwable cause) {
        super(cause);
    }

}
