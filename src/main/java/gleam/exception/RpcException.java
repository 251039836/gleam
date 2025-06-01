package gleam.exception;

/**
 * rpc调用相关的错误
 * 
 * @author hdh
 *
 */
public class RpcException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2030708813599101449L;

    public RpcException() {
    }

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

}
