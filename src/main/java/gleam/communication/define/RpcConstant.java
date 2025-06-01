package gleam.communication.define;

import java.util.concurrent.TimeUnit;

public class RpcConstant {

	/**
	 * 默认rpc超时时间
	 */
	public final static long DEFAULT_RPC_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
	/**
	 * 回调过期检查间隔<br>
	 * 毫秒
	 */
	public final static long CALLBACK_EXPIRED_CHECK_INTERVAL = 250;

}
