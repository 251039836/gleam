package gleam.communication.server.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.authenticate.Identity;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.define.ConnectionConstant;
import gleam.communication.define.DisconnectReason;
import gleam.communication.server.Acceptor;
import gleam.communication.server.ConnectionManager;
import gleam.communication.server.Server;
import gleam.communication.server.ServerConnectionListener;
import gleam.communication.task.CommunicationTaskManager;
import gleam.core.service.AbstractService;
import gleam.task.TaskHandle;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * socket长连接服务端
 * 
 * @author hdh
 */
public abstract class SocketServer extends AbstractService implements Server {
	/**
	 * 链接管理类
	 */
	protected final ConnectionManager connectionManager;
	/**
	 * 服务器id
	 */
	protected int id;
	/**
	 * 服务器类型
	 */
	protected IdentityType type;
	/**
	 * 监听地址
	 */
	protected InetSocketAddress address;
	/**
	 * 接收器
	 */
	protected Acceptor acceptor;
	/**
	 * tick任务<br>
	 * 用于发送心跳/断线重连
	 */
	protected TaskHandle tickTask;

	public SocketServer() {
		this.connectionManager = buildConnectionManger();
	}

	@Override
	public int getPriority() {
		return PRIORITY_LOWEST;
	}

	/**
	 * 服务器id
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 获取服务器类型
	 * 
	 * @return
	 */
	public IdentityType getType() {
		return type;
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public void onInitialize() {
		this.id = owner.getServerId();
		this.type = owner.getServerType();
		this.address = loadAddress();
		ServerConnectionListener<?> connectionListener = buildConnectionListener();
		connectionListener.init();
		ChannelInitializer<SocketChannel> channelInitializer = buildChannelInitializer(connectionListener);
		this.acceptor = buildAcceptor(channelInitializer);
	}

	@Override
	public void onStart() {
		String threadName = getName() + "_Acceptor";
		Thread thread = new Thread(() -> {
			acceptor.listen();
		}, threadName);
		thread.start();
		// 阻塞到成功监听
		try {
			while (!acceptor.isActive()) {
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			logger.error(getName() + " startup error.", e);
			System.exit(1);
		}
		// 开启定时任务 清理过期连接
		startTickTask();
	}

	@Override
	public void onStop() {
		cancelTickTask();
		if (acceptor != null) {
			acceptor.shutdown();
		}
	}

	protected void startTickTask() {
		cancelTickTask();
		tickTask = CommunicationTaskManager.TIMER.scheduleTask(() -> {
			tick();
		}, ConnectionConstant.TICK_INTERVAL, ConnectionConstant.TICK_INTERVAL);
	}

	protected void cancelTickTask() {
		if (tickTask != null) {
			tickTask.cancel();
		}
		tickTask = null;
	}

	protected void tick() {
		// 清理过期连接
		checkTimeoutConnections();
	}

	protected void checkTimeoutConnections() {
		long now = System.currentTimeMillis();
		long limitTime = now - getHeartbeatTimeoutTime();
		ConcurrentMap<String, Connection> connectionMap = connectionManager.getConnectionMap();
		for (Connection connection : connectionMap.values()) {
			if (!connection.isActive()) {
				// 已断开链接
				continue;
			}
			long creationTime = connection.getCreationTime();
			if (creationTime > limitTime) {
				// 建立的连接时间未超过1个心跳周期时 不检查
				continue;
			}
			if (connection.getIdentity() == null) {
				// 若链接1个心跳周期还未注册
				connection.close(DisconnectReason.AUTH_TIMEOUT);
				continue;
			}
			if (connection.getHeartbeatTime() < limitTime) {
				// 心跳过期
				connection.close(DisconnectReason.HEARTBEAT_TIMEOUT);
				continue;
			}
		}
	}

	/**
	 * 获取心跳过期时间
	 * 
	 * @return
	 */
	protected long getHeartbeatTimeoutTime() {
		return ConnectionConstant.DEFAULT_HEARTBEAT_TIMEOUT;
	}

	/**
	 * 创建接收器
	 * 
	 * @param channelInitializer
	 * @return
	 */
	protected Acceptor buildAcceptor(ChannelInitializer<SocketChannel> channelInitializer) {
		InetSocketAddress address = getAddress();
		String name = getName();
		Acceptor acceptor = new SocketAcceptor(name, address, channelInitializer);
		return acceptor;
	}

	@Override
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	@Override
	public Acceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}

	protected ConnectionManager buildConnectionManger() {
		return new ConnectionManager();
	}

	/**
	 * 创建连接初始化类
	 * 
	 * @param connectionListener
	 * @return
	 */
	protected ChannelInitializer<SocketChannel> buildChannelInitializer(
			ServerConnectionListener<?> connectionListener) {
		SocketChannelInitializer initializer = new SocketChannelInitializer(connectionListener);
		return initializer;
	}

	/**
	 * 创建连接监听器
	 * 
	 * @return
	 */
	protected abstract ServerConnectionListener<?> buildConnectionListener();

	/**
	 * 创建监听地址<br>
	 * init时执行
	 * 
	 * @return
	 */
	protected abstract InetSocketAddress loadAddress();

	/**
	 * 广播协议给满足条件的链接<br>
	 * {@link DefaultChannelGroup#writeAndFlush(Object)}
	 * 
	 * @param protocol
	 * @param predicate
	 */
	public void broadcast(Protocol protocol, Predicate<Identity> predicate) {
		if (predicate == null) {
			broadcastAll(protocol);
			return;
		}
		ConcurrentMap<Identity, Connection> identityConnectionMap = connectionManager.getIdentityConnectionMap();
		if (identityConnectionMap.isEmpty()) {
			return;
		}
		BinaryWebSocketFrame frame = buildBroadcastFrame(protocol);
		for (Connection connection : identityConnectionMap.values()) {
			Identity identity = connection.getIdentity();
			if (predicate.test(identity)) {
				BinaryWebSocketFrame tmpFrame = frame.retainedDuplicate();
				connection.sendMessage(tmpFrame);
			}
		}
		frame.release();
	}

	/**
	 * 广播协议给所有链接<br>
	 * 含未验证/未登录的链接<br>
	 * {@link DefaultChannelGroup#writeAndFlush(Object)}
	 * 
	 * @param protocol
	 */
	public void broadcastAll(Protocol protocol) {
		ConcurrentMap<String, Connection> connectionMap = connectionManager.getConnectionMap();
		if (connectionMap.isEmpty()) {
			return;
		}
		BinaryWebSocketFrame frame = buildBroadcastFrame(protocol);
		for (Connection connection : connectionMap.values()) {
			BinaryWebSocketFrame tmpFrame = frame.retainedDuplicate();
			connection.sendMessage(tmpFrame);
		}
		frame.release();
	}

	protected BinaryWebSocketFrame buildBroadcastFrame(Protocol protocol) {
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
		ByteBuf buffer = frame.content();
		int seq = protocol.getSeq();
		int id = protocol.getId();
		buffer.writeInt(seq);// 序号
		buffer.writeInt(id);// 协议id
		protocol.encode(buffer);// 协议内容
		return frame;
	}
}
