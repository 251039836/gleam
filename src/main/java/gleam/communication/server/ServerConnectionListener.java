package gleam.communication.server;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.define.ConnectionConstant;
import gleam.communication.impl.AbstractConnectionListener;
import gleam.communication.server.impl.SocketServer;
import gleam.exception.ServerStarupError;

/**
 * 服务端链接监听器
 * 
 * @author hdh
 *
 */
public abstract class ServerConnectionListener<T extends SocketServer> extends AbstractConnectionListener {

	protected final T server;

	protected final ConnectionManager connectionManager;

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

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public T getServer() {
		return server;
	}

	@Override
	protected void registerDirectHandler(MessageDirectHandler<?> messageHandler) {
		int msgId = messageHandler.getReqId();
		MessageDirectHandler<?> otherHandler = directHandlers.put(msgId, messageHandler);
		if (otherHandler != null) {
			logger.error("register base message handler error.msgId[{}] register repeated.", msgId);
			throw new ServerStarupError("register base message handler error.msgId[" + msgId + "] register repeated.");
		}
	}

}
