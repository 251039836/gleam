package gleam.communication;

import java.util.concurrent.Future;

import gleam.communication.authenticate.Identity;
import gleam.communication.define.DisconnectReason;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.AttributeKey;

/**
 * 链接<br>
 * 网络链接的封装<br>
 * 
 * @author hdh
 *
 */
public interface Connection {

	AttributeKey<Connection> ATTR_KEY = AttributeKey.valueOf(Connection.class.getSimpleName());

	/**
	 * 获取参数值
	 * 
	 * @param key
	 * @return 若不存在 则返回null
	 */
	<T> T getAttribute(AttributeKey<T> key);

	/**
	 * 获取链接创建时间
	 * 
	 * @return
	 */
	long getCreationTime();

	/**
	 * 获取最后次心跳时间
	 * 
	 * @return
	 */
	long getHeartbeatTime();

	/**
	 * 唯一id
	 * 
	 * @return
	 */
	String getId();

	/**
	 * 获取身份
	 * 
	 * @return
	 */
	Identity getIdentity();

	/**
	 * 远端ip地址
	 * 
	 * @return
	 */
	String getRemoteIp();

	/**
	 * 是否链接中
	 * 
	 * @return
	 */
	boolean isActive();

	/**
	 * 是否已断开链接
	 * 
	 * @return
	 */
	boolean isClose();

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	void sendMessage(ByteBuf message);

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	void sendMessage(ByteBufHolder message);

	/**
	 * 发送协议
	 * 
	 * @param protocol
	 */
	void sendProtocol(Protocol protocol);

	/**
	 * 发送协议并断开链接
	 * 
	 * @param protocol
	 * @param reason   {@link DisconnectReason}
	 */
	void sendProtocolAndClose(Protocol protocol, int reason);

	/**
	 * 断开链接<br>
	 * 
	 * @param reason {@link DisconnectReason}
	 * @return
	 */
	Future<Void> close(int reason);

	/**
	 * 设置参数值
	 * 
	 * @param key
	 * @param value
	 */
	<T> void setAttribute(AttributeKey<T> key, T value);

	/**
	 * 最后次心跳时间
	 * 
	 * @param heartbeatTime
	 */
	void setHeartbeatTime(long heartbeatTime);

	/**
	 * 设置身份
	 * 
	 * @param identity
	 */
	void setIdentity(Identity identity);

	/**
	 * 获取链接全名
	 * 
	 * @return
	 */
	String toFullName();

}
