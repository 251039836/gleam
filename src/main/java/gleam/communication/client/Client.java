package gleam.communication.client;

import java.net.InetSocketAddress;
import java.util.Collection;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.core.service.Service;

/**
 * 客户端<br>
 * 用于主动发起链接目标服务器
 * 
 * @author hdh
 *
 */
public interface Client extends Service {

	/**
	 * 连接器
	 * 
	 * @return
	 */
	Connector getConnector();

	/**
	 * 客户端自己的身份
	 * 
	 * @return
	 */
	InnerIdentity getLocalIdentity();

	/**
	 * 远程服务器的身份
	 * 
	 * @return
	 */
	InnerIdentity getRemoteIdentity();

	/**
	 * 远程地址
	 * 
	 * @return
	 */
	InetSocketAddress getRemoteAddress();

	/**
	 * 当前连接是否有效
	 * 
	 * @return
	 */
	boolean isActive();

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	void sendMessage(Protocol message);

	/**
	 * 转发消息到目标服务器上
	 * 
	 * @param dstServerType
	 * @param dstServerId
	 * @param message
	 */
	void forwardMessage(int dstServerType, int dstServerId, Protocol message);

	/**
	 * 转发消息到多个服务器上
	 *
	 * @param dstServerType
	 * @param dstServerIds
	 * @param message
	 */
	void forwardMessage(int dstServerType, Collection<Integer> dstServerIds, Protocol message);

	/**
	 * 获取连接
	 * 
	 * @return
	 */
	Connection getConnection();

	/**
	 * 设置连接
	 * 
	 * @param connection
	 */
	void setConnection(Connection connection);

}
