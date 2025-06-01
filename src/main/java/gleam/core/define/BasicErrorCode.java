package gleam.core.define;

public class BasicErrorCode {

	/**
	 * 无效连接
	 */
	public final static int INVALID_CONNECT = 10101;
	/**
	 * 实体不存在
	 */
	public final static int ENTITY_NOT_EXISTS = 10102;
	/**
	 * 协议无法解析
	 */
	public final static int PROTOCOL_CANT_DECODE = 10103;
	/**
	 * rpc超时
	 */
	public final static int RPC_TIMEOUT = 10104;
	/**
	 * 目标服务器无法到达
	 */
	public final static int DST_SERVER_CANT_REACH = 10105;

	/**
	 * 未知异常错误码
	 */
	public final static int UNKNOW_ERROR = 11001;
}
