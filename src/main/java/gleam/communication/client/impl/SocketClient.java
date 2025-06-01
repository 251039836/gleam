package gleam.communication.client.impl;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.client.Client;
import gleam.communication.client.ClientConnectionListener;
import gleam.communication.client.Connector;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.inner.protocol.ReqInnerForward;
import gleam.communication.inner.protocol.ReqInnerHeartbeat;
import gleam.communication.inner.protocol.ReqMultipleServerForward;
import gleam.communication.protocol.ProtocolUtil;
import gleam.communication.task.CommunicationTaskManager;
import gleam.core.service.AbstractService;
import gleam.task.TaskHandle;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * socket长连接客户端
 * 
 * @author hdh
 *
 */
public abstract class SocketClient extends AbstractService implements Client {
	/**
	 * tick任务间隔<br>
	 * 心跳间隔 或 断线重连检查时间<br>
	 */
	public final static long TICK_INTERVAL = TimeUnit.SECONDS.toMillis(5);
	/**
	 * 该客户端的身份
	 */
	protected final InnerIdentity localIdentity;
	/**
	 * 远程服务器的身份
	 */
	protected final InnerIdentity remoteIdentity;
	/**
	 * 远程服务器地址
	 */
	protected final InetSocketAddress remoteAddress;
	/**
	 * 连接器
	 */
	protected Connector connector;
	/**
	 * 当前链接
	 */
	protected Connection connection;
	/**
	 * tick任务<br>
	 * 用于发送心跳/断线重连
	 */
	protected TaskHandle tickTask;

	public SocketClient(InnerIdentity localIdentity, InnerIdentity remoteIdentity, InetSocketAddress remoteAddress) {
		this.localIdentity = localIdentity;
		this.remoteIdentity = remoteIdentity;
		this.remoteAddress = remoteAddress;
	}

	@Override
	public void onInitialize() {
		ClientConnectionListener<?> connectionListener = buildConnectionListener();
		connectionListener.init();
		ChannelInitializer<SocketChannel> channelInitializer = buildChannelInitializer(connectionListener);
		this.connector = buildConnector(channelInitializer);
	}

	@Override
	public void onStart() {
		connector.connect();
		// 心跳/断线重连检查
		startTickTask();
	}

	@Override
	public void onStop() {
		cancelTickTask();
		if (connector != null) {
			connector.close();
		}
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String getName() {
		String name = localIdentity.getKey() + "-" + remoteIdentity.getKey();
		return name;
	}

	protected void startTickTask() {
		cancelTickTask();
		tickTask = CommunicationTaskManager.TIMER.scheduleTask(() -> {
			tick();
		}, TICK_INTERVAL, TICK_INTERVAL);
	}

	protected void cancelTickTask() {
		if (tickTask != null) {
			tickTask.cancel();
		}
		tickTask = null;
	}

	protected void tick() {
		checkAndReconnect();
		// 心跳包
		if (connector.isActive()) {
			sendHeartbeat();
		}
	}

	protected void sendHeartbeat() {
		ReqInnerHeartbeat heartbeat = new ReqInnerHeartbeat();
		connection.sendProtocol(heartbeat);
	}

	public void checkAndReconnect() {
		if (connector.isActive()) {
			// 连接有效
			return;
		}
		// 尝试断线重连
		// 链接已断开 尝试重连
		connector.reconnect();
	}

	@Override
	public void sendMessage(Protocol message) {
		if (connection == null || !connection.isActive()) {
			logger.warn("send message error.client[{}] is not active.", getName());
			return;
		}
		connection.sendProtocol(message);
	}

	@Override
	public void forwardMessage(int dstServerType, int dstServerId, Protocol message) {
		if (connection == null || !connection.isActive()) {
			logger.warn("send message error.client[{}] is not active.", getName());
			return;
		}
		ReqInnerForward forwardMsg = new ReqInnerForward();
		forwardMsg.setSrcServerType(localIdentity.getType().getType());
		forwardMsg.setSrcServerId(localIdentity.getId());
		forwardMsg.setDstServerType(dstServerType);
		forwardMsg.setDstServerId(dstServerId);
		forwardMsg.setForwardMsgId(message.getId());
		forwardMsg.setForwardMsgSeq(message.getSeq());
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(forwardMsg);
		forwardMsg.setForwardMsgData(forwardMsgData);
		connection.sendProtocol(forwardMsg);
	}

	@Override
	public void forwardMessage(int dstServerType, Collection<Integer> dstServerIds, Protocol message) {
		if (connection == null || !connection.isActive()) {
			logger.warn("send message error.client[{}] is not active.", getName());
			return;
		}
		ReqMultipleServerForward forwardMsg = new ReqMultipleServerForward();
		forwardMsg.setSrcServerType(localIdentity.getType().getType());
		forwardMsg.setSrcServerId(localIdentity.getId());
		forwardMsg.setDstServerType(dstServerType);
		forwardMsg.getDstServerIds().addAll(dstServerIds);
		forwardMsg.setForwardMsgId(message.getId());
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(message);
		forwardMsg.setForwardMsgData(forwardMsgData);
		connection.sendProtocol(forwardMsg);
	}

	@Override
	public InnerIdentity getLocalIdentity() {
		return localIdentity;
	}

	@Override
	public InnerIdentity getRemoteIdentity() {
		return remoteIdentity;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public boolean isActive() {
		if (connector == null) {
			return false;
		}
		return connector.isActive();
	}

	protected Connector buildConnector(ChannelInitializer<SocketChannel> channelInitializer) {
		Connector connector = new SocketConnector(remoteIdentity, remoteAddress, channelInitializer);
		return connector;
	}

	/**
	 * 创建连接初始化类
	 * 
	 * @param connectionListener
	 * @return
	 */
	protected abstract ChannelInitializer<SocketChannel> buildChannelInitializer(
			ClientConnectionListener<?> connectionListener);

	/**
	 * 创建连接监听器
	 * 
	 * @return
	 */
	protected abstract ClientConnectionListener<?> buildConnectionListener();

}
