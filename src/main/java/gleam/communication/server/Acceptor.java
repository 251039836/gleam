package gleam.communication.server;

/**
 * 接收器
 * 
 * @author hdh
 *
 */
public interface Acceptor {
    /**
     * 监听的域名
     * 
     * @return
     */
    String getHost();

    /**
     * 接收器名
     * 
     * @return
     */
    String getName();

    /**
     * 监听的端口
     * 
     * @return
     */
    int getPort();

    /**
     * 是否激活状态<br>
     * 是否已监听
     * 
     * @return
     */
    boolean isActive();

    /**
     * 开始监听
     */
    void listen();

    /**
     * 关闭
     */
    void shutdown();

}
