package gleam.communication.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.ConnectionListener;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.task.HandleMsgTask;
import gleam.core.Entity;

public abstract class AbstractConnectionListener implements ConnectionListener {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final Map<Integer, MessageDirectHandler<?>> directHandlers = new HashMap<>();

	public void init() {
	}

	protected void registerDirectHandler(MessageDirectHandler<? extends Protocol> messageHandler) {
		int msgId = messageHandler.getReqId();
		MessageDirectHandler<?> otherHandler = directHandlers.put(msgId, messageHandler);
		if (otherHandler != null) {
			logger.error("register message direct handler error.msgId[{}] register repeated.", msgId);
		}
	}

	@Override
	public void exceptionCaught(Connection connection, Throwable cause) {
		logger.error("connection[{}] exceptionCaught:{}:{}", connection.toFullName(), cause.getClass().getName(),
				cause.getMessage());
	}

	@Override
	public void handleTriggerEvent(Connection connection, Object event) {

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		// 默认netty一条链接对应单个线程
		// 内网通信时 需改为多线程调用
		int msgId = protocol.getId();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		int seq = protocol.getSeq();
		if (messageHandler != null) {
			Protocol response = messageHandler.handleMessage(protocol);
			if (response != null) {
				response.setSeq(-seq);
				connection.sendProtocol(response);
			}
		}
		Entity<?> entity = getHandleEntity(connection, protocol);
		if (entity != null) {
			// 线程池;
			entityHandleMsg(entity, connection, protocol);
			return;
		}
		handleMsgWithoutEntity(connection, protocol);
	}

	protected abstract Entity<?> getHandleEntity(Connection connection, Protocol protocol);

	protected void entityHandleMsg(Entity<?> entity, Connection connection, Protocol protocol) {
		HandleMsgTask task = HandleMsgTask.get(entity, connection, protocol);
		entity.submitTask(task);
	}

	protected void handleMsgWithoutEntity(Connection connection, Protocol protocol) {
		logger.warn("connection[{}] handle protocol[{}] seq[{}] error.messageHandler not exists.",
				connection.toFullName(), protocol.getClass().getName(), protocol.getSeq());

	}

	public Map<Integer, MessageDirectHandler<?>> getDirectHandlers() {
		return directHandlers;
	}
}
