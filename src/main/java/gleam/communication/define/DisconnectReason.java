package gleam.communication.define;

/**
 * 断开链接的原因
 * 
 * @author hdh
 *
 */
public class DisconnectReason {
	/**
	 * 相同身份
	 */
	public final static int SAME_IDENTITY = 1;
	/**
	 * 身份验证超时
	 */
	public final static int AUTH_TIMEOUT = 2;
	/**
	 * 心跳超时
	 */
	public final static int HEARTBEAT_TIMEOUT = 3;
	/**
	 * 身份验证失败
	 */
	public final static int AUTH_FAIL = 4;
	/**
	 * 解码出错
	 */
	public final static int DECODE_ERROR = 5;
	/**
	 * 未验证
	 */
	public final static int NO_AUTH = 6;
	/**
	 * 客户端主动关闭
	 */
	public final static int CLIENT = 7;
	/**
	 * 服务器维护<br>
	 * 网关中的该逻辑器状态为维护
	 */
	public final static int MAINTAIN = 8;
	/**
	 * 协议发送过于频繁
	 */
	public final static int OVERSPEED = 9;
	/**
	 * 服务端强行关闭
	 */
	public final static int FORCE = 10;
	/**
	 * 登陆失败
	 */
	public final static int LOGIN_ERROR = 11;
	/**
	 * 防沉迷登陆失败
	 */
	public final static int LOGIN_VERIFY = 12;

	/**
	 * 用于服务端和后台/大数据/运维的通信<br>
	 * 消息处理结束 关闭socket链接
	 */
	public final static int OVER = 14;
}
