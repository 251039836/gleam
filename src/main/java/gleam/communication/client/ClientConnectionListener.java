package gleam.communication.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.ConnectionListener;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.define.ConnectionConstant;
import gleam.core.define.ServiceStatus;

public abstract class ClientConnectionListener<T extends Client> implements ConnectionListener {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final T client;

	protected final Map<Integer, MessageDirectHandler<?>> directHandlers = new HashMap<>();

	public ClientConnectionListener(T client) {
		super();
		this.client = client;
	}

	protected void registerDirectHandler(MessageDirectHandler<? extends Protocol> messageHandler) {
		int msgId = messageHandler.getReqId();
		MessageDirectHandler<?> otherHandler = directHandlers.put(msgId, messageHandler);
		if (otherHandler != null) {
			logger.error("register message direct handler error.msgId[{}] register repeated.", msgId);
		}
	}

	@Override
	public void handleTriggerEvent(Connection connection, Object event) {

	}

	@Override
	public void connected(Connection connection) {
		// 客户端连接成功
		client.setConnection(connection);
		logger.info("client[{}] connected.", client.getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		// netty默认单链接对应单线程处理
		int msgId = protocol.getId();
		int seq = protocol.getSeq();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		if (messageHandler != null) {
			Protocol response = messageHandler.handleMessage(protocol);
			if (response != null) {
				response.setSeq(-seq);
				connection.sendProtocol(response);
			}
		} else {
			logger.warn("connection[{}] handle protocol[{}] seq[{}] error.messageHandler not exists.",
					connection.toFullName(), protocol.getClass().getName(), seq);
		}
	}

	@Override
	public void exceptionCaught(Connection connection, Throwable cause) {
		logger.error("connection[{}] exceptionCaught:{}", connection.toFullName(), cause.getMessage());
	}

	@Override
	public void disconnected(Connection connection) {
		Integer closeReason = connection.getAttribute(ConnectionConstant.CLOSE_REASON_ATTR_KEY);
		logger.warn("client[{}] disconnected.closeReason={}", client.getName(), closeReason);
		if (client.getStatus() == ServiceStatus.STARTED) {
			// 若该客户端还未关闭 则尝试重连
			client.getConnector().reconnect();
		}
	}

	public T getClient() {
		return client;
	}

	public Map<Integer, MessageDirectHandler<?>> getDirectHandlers() {
		return directHandlers;
	}

}
