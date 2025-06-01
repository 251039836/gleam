package gleam.communication.client;

import java.net.InetSocketAddress;

import gleam.communication.authenticate.Identity;

/**
 * 连接器<br>
 * 主动连接目标地址
 * 
 * @author hdh
 */
public interface Connector {
    /** 初始化 */
    int STATUS_INIT = 0;
    /** 连接中(未连上) */
    int STATUS_CONNECTING = 1;
    /** 已连接 */
    int STATUS_CONNECTED = 2;
    /** 断开链接 */
    int STATUS_DISCONNECT = 3;
    /** 关闭(主动关闭 不再重连) */
    int STATUS_CLOSE = 4;

    /**
     * 关闭<br>
     * 主动关闭连接
     */
    void close();

    /**
     * 连接
     */
    void connect();

    /**
     * 要链接的端的域名
     * 
     * @return
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 要链接的端的身份
     * 
     * @return
     */
    Identity getRemoteIdentity();

    /**
     * 获取连接器状态
     * 
     * @return
     */
    int getStatus();

    /**
     * 当前连接是否有效
     * 
     * @return
     */
    boolean isActive();

    /**
     * 重连
     */
    void reconnect();

}
