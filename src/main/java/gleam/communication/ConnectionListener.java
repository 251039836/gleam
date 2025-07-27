package gleam.communication;

/**
 * 链接监听器<br>
 * 监听并处理链接相关事件/消息
 * 
 * @author hdh
 */
public interface ConnectionListener {

    void init() throws Exception;

    /**
     * 连接成功事件
     * 
     * @param connection
     */
    void connected(Connection connection);

    /**
     * 链接已断开
     * 
     * @param connection
     */
    void disconnected(Connection connection);

    /**
     * 捕获到错误
     * 
     * @param connection
     * @param cause
     */
    void exceptionCaught(Connection connection, Throwable cause);

    /**
     * 处理链接触发的事件
     * 
     * @param connection
     * @param event
     */
    void handleTriggerEvent(Connection connection, Object event);

    /**
     * 接受到协议
     * 
     * @param connection
     * @param message
     */
    void receiveProtocol(Connection connection, Protocol protocol);
}
