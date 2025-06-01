package gleam.communication.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.ConnectionListener;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.define.ConnectionConstant;
import gleam.communication.server.impl.SocketServer;
import gleam.exception.ServerStarupError;

/**
 * 服务端链接监听器
 * 
 * @author hdh
 *
 */
public abstract class ServerConnectionListener<T extends SocketServer> implements ConnectionListener {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final T server;

	protected final ConnectionManager connectionManager;

	protected final Map<Integer, MessageDirectHandler<?>> directHandlers = new HashMap<>();

	public ServerConnectionListener(T server) {
		this.server = server;
		this.connectionManager = server.getConnectionManager();
	}

	@Override
	public void connected(Connection connection) {
		connectionManager.addConnection(connection);
		logger.info("connection[{}] connected", connection.toFullName());
	}

	@Override
	public void disconnected(Connection connection) {
		connectionManager.removeConnection(connection);
		Integer closeReason = connection.getAttribute(ConnectionConstant.CLOSE_REASON_ATTR_KEY);
		logger.info("connection[{}] disconnected.closeReason={}", connection.toFullName(), closeReason);
	}

	@Override
	public void exceptionCaught(Connection connection, Throwable cause) {
		logger.error("connection[{}] exceptionCaught:{}:{}", connection.toFullName(), cause.getClass().getName(),
				cause.getMessage());
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public Map<Integer, MessageDirectHandler<?>> getDirectHandlers() {
		return directHandlers;
	}

	public T getServer() {
		return server;
	}

	@Override
	public void handleTriggerEvent(Connection connection, Object event) {

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		int msgId = protocol.getId();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		if (messageHandler != null) {
			Protocol response = messageHandler.handleMessage(protocol);
			if (response != null) {
				// 默认返回seq为请求seq
				response.setSeq(protocol.getSeq());
				connection.sendProtocol(response);
			}
		} else {
			logger.warn("connection[{}] handle protocol[{}] seq[{}] error.messageHandler not exists.",
					connection.toFullName(), protocol.getClass().getName(), protocol.getSeq());
		}
	}

	protected void registerDirectHandler(MessageDirectHandler<?> messageHandler) {
		int msgId = messageHandler.getReqId();
		MessageDirectHandler<?> otherHandler = directHandlers.put(msgId, messageHandler);
		if (otherHandler != null) {
			logger.error("register base message handler error.msgId[{}] register repeated.", msgId);
			throw new ServerStarupError("register base message handler error.msgId[" + msgId + "] register repeated.");
		}
	}

}
