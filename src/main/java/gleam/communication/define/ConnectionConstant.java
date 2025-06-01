package gleam.communication.define;

import java.util.concurrent.TimeUnit;

import io.netty.util.AttributeKey;

/**
 * 连接相关常量
 * 
 * @author hdh
 *
 */
public class ConnectionConstant {
	/**
	 * 真实ip<br>
	 * 使用nginx后 无法直接获取真实ip
	 */
	public final static AttributeKey<String> REAL_IP_ATTR_KEY = AttributeKey.valueOf("ip");
	/**
	 * 关闭链接原因
	 */
	public final static AttributeKey<Integer> CLOSE_REASON_ATTR_KEY = AttributeKey.valueOf("closeReason");
	/**
	 * tick任务间隔<br>
	 * 判断心跳过期/断线重连检查时间
	 */
	public final static long TICK_INTERVAL = TimeUnit.SECONDS.toMillis(10);
	/**
	 * 默认心跳间隔
	 */
	public final static long DEFAULT_HEARTBEAT_INTERVAL = TimeUnit.SECONDS.toMillis(10);
	/**
	 * 默认心跳过期时间<br>
	 * 超时未注册/发送心跳的 关闭连接
	 */
	public final static long DEFAULT_HEARTBEAT_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
	/**
	 * 实名验证生日
	 */
	public final static AttributeKey<String> VERIFY_BIRTH = AttributeKey.valueOf("birth");
	/**
	 * 实名验证账号月充值金额
	 */
	public final static AttributeKey<Integer> MONTH_PAY = AttributeKey.valueOf("monthPay");

}
