package gleam.communication.websocket.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.define.ConnectionConstant;
import gleam.communication.define.DisconnectReason;
import gleam.communication.server.ServerConnectionListener;
import gleam.communication.websocket.WebSocketServer;
import gleam.communication.websocket.define.WebSocketConstant;
import gleam.communication.websocket.monitor.ProtocolMonitor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;

public abstract class WebSocketServerConnectionListener<T extends WebSocketServer> extends ServerConnectionListener<T> {

	public WebSocketServerConnectionListener(T server) {
		super(server);
	}

	@Override
	public void connected(Connection connection) {
		connectionManager.addConnection(connection);
		// 添加协议监控
		ProtocolMonitor monitor = new ProtocolMonitor();
		connection.setAttribute(ProtocolMonitor.ATTR_KEY, monitor);
		logger.debug("channel[{}] connected.", connection.toFullName());
	}

	/**
	 * 处理连接协议发送超速
	 * 
	 * @param connection
	 * @param monitor
	 */
	protected void handleConnectionOverspeed(Connection connection, ProtocolMonitor monitor) {
		// 打印协议发送数前5的内容
		List<Integer> largerProtocolIds = monitor.getLargerProtocolIds(5);
		connection.close(DisconnectReason.OVERSPEED);
		logger.warn("connection[{}] send protocol overspeed.largerProtocolIds=[{}]", connection.toFullName(),
				StringUtils.join(largerProtocolIds, ","));
	}

	private void handleHandshakeComplete(Connection connection, HandshakeComplete handshakeComplete) {
		HttpHeaders headers = handshakeComplete.requestHeaders();
		String realIp = headers.get(WebSocketConstant.HEADER_REAL_IP);
		if (realIp == null || realIp.isEmpty()) {
			realIp = headers.get(WebSocketConstant.HEADER_FORWARDED_FOR);
		}
		if (realIp != null && !realIp.isEmpty()) {
			connection.setAttribute(ConnectionConstant.REAL_IP_ATTR_KEY, realIp);
		}
		logger.info("connection[{}] handshake.", connection.toFullName());
	}

	@Override
	public void handleTriggerEvent(Connection connection, Object event) {
		if (event instanceof HandshakeComplete) {
			HandshakeComplete handshakeComplete = (HandshakeComplete) event;
			handleHandshakeComplete(connection, handshakeComplete);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		// 监控玩家发送协议频率
		long now = System.currentTimeMillis();
		ProtocolMonitor monitor = connection.getAttribute(ProtocolMonitor.ATTR_KEY);
		boolean overspeed = monitor.receiveProtocol(protocol.getId(), now);
		if (overspeed) {
			handleConnectionOverspeed(connection, monitor);
			return;
		}
		int msgId = protocol.getId();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		Protocol response = null;
		if (messageHandler != null) {
			response = messageHandler.handleMessage(protocol);
		} else {
			response = handleCustomProtocol(connection, protocol);
		}
		if (response != null) {
			response.setSeq(protocol.getSeq());
			connection.sendProtocol(response);
		}
	}

	/**
	 * 处理自定义协议
	 * 
	 * @param connection
	 * @param protocol
	 * @return
	 */
	protected abstract Protocol handleCustomProtocol(Connection connection, Protocol protocol);

}
