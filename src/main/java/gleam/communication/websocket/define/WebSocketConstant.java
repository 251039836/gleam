package gleam.communication.websocket.define;

import java.util.concurrent.TimeUnit;

/**
 * websocket相关常量<br>
 * 用于前端和服务端之间的通信
 * 
 * @author hdh
 *
 */
public class WebSocketConstant {

    public final static int PROTOCOL_SEQ_FIELD_LENGTH = 4;

    public final static int PROTOCOL_ID_FIELD_LENGTH = 4;
    /**
     * 协议头部长度<br>
     * 序号+id<br>
     */
    public final static int PROTOCOL_HEADER_LENGTH = PROTOCOL_SEQ_FIELD_LENGTH + PROTOCOL_ID_FIELD_LENGTH;

    public final static String HEADER_REAL_IP = "X-real-ip";

    public final static String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * 前端websocket心跳间隔<br>
     * 很奇怪的一个值
     */
    public final static long HEARTBEAT_INTERVAL = TimeUnit.SECONDS.toMillis(32);
    /**
     * websocket心跳过期时间<br>
     * 超时未注册/发送心跳的 关闭连接
     */
    public final static long HEARTBEAT_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

    /**
     * websocket心跳警告时间<br>
     */
    public final static long HEARTBEAT_TIMEOUT_WARNTIME = TimeUnit.SECONDS.toMillis(110);

}
