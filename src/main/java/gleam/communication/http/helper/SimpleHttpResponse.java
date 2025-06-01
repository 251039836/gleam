package gleam.communication.http.helper;

/**
 * 简单http返回结构
 * 
 * @author hdh
 */
public class SimpleHttpResponse {
	/**
	 * 成功错误码
	 */
	private static final int ERROR_CODE_SUCCESS = 0;
	/**
	 * 失败错误码
	 */
	private static final int ERROR_CODE_FAILED = -1;

	/**
	 * 构造失败消息，内容为空
	 * 
	 * @return
	 */
	public static SimpleHttpResponse failed() {
		return failed(null);
	}

	/**
	 * 构造完整成功消息
	 * 
	 * @param msg 协议的返还内容
	 * @return JsonHandlerResp
	 */
	public static SimpleHttpResponse failed(Object msg) {
		return new SimpleHttpResponse(ERROR_CODE_FAILED, msg);
	}

	/**
	 * 构造成功消息，内容为空
	 * 
	 * @return
	 */
	public static SimpleHttpResponse success() {
		return success(null);
	}

	/**
	 * 构造完整失败消息
	 * 
	 * @param msg 协议的返还内容
	 * @return JsonHandlerResp
	 */
	public static SimpleHttpResponse success(Object msg) {
		return new SimpleHttpResponse(ERROR_CODE_SUCCESS, msg);
	}

	/**
	 * 错误码
	 */
	private final int code;

	/**
	 * 协议回应的其他消息，没有传 null
	 */
	private final Object msg;

	private SimpleHttpResponse(int code, Object msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public Object getMsg() {
		return msg;
	}

}
