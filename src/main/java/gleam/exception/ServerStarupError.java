package gleam.exception;

/**
 * 服务器启动失败错误<br>
 * 服务初始化/启动失败时抛出
 * 
 * @author hdh
 *
 */
public class ServerStarupError extends Error {

    /**
     * 
     */
    private static final long serialVersionUID = 2056379232008015588L;

    public ServerStarupError(String message) {
        super(message);
    }

    public ServerStarupError(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerStarupError(Throwable cause) {
        super(cause);
    }
}
