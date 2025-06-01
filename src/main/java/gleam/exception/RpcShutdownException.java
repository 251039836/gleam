package gleam.exception;

/**
 * rpc客户端关闭异常
 * 
 * @author hdh
 *
 */
public class RpcShutdownException extends RpcException {

    /**
     * 
     */
    private static final long serialVersionUID = 185074380391181783L;

    public RpcShutdownException() {
    }

    public RpcShutdownException(String msg) {
        super(msg);
    }

    public RpcShutdownException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcShutdownException(Throwable cause) {
        super(cause);
    }

}
