package gleam.communication.inner;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.handler.ReqInnerAuthenticateHandler;
import gleam.communication.inner.handler.ReqInnerForwardHandler;
import gleam.communication.inner.handler.ReqInnerHeartbeatHandler;
import gleam.communication.inner.handler.ReqInnerRpcForwardRelayHandler;
import gleam.communication.inner.handler.ReqMultipleServerForwardHandler;
import gleam.communication.rpc.ConnectionRpcAddon;
import gleam.communication.rpc.RpcCallbackCache;
import gleam.communication.server.ServerConnectionListener;
import gleam.communication.task.CommunicationTaskManager;
import gleam.core.service.Context;

/**
 * 内网连接中的服务端端的连接监听<br>
 * 支持服务端向客户端方向的rpc调用
 * 
 * @author hdh
 *
 */
public abstract class InnerServerConnectionListener<T extends InnerServer> extends ServerConnectionListener<T> {

	public InnerServerConnectionListener(T server) {
		super(server);
	}

	@Override
	public void init() {
		registerInnerBasicHandlers();
	}

	protected void registerInnerBasicHandlers() {
		ReqInnerAuthenticateHandler authHandler = new ReqInnerAuthenticateHandler(server);
		ReqInnerHeartbeatHandler heartbeatHandler = new ReqInnerHeartbeatHandler();
		ReqInnerForwardHandler forwardHandler = new ReqInnerForwardHandler(server);
		ReqMultipleServerForwardHandler multipleServerForwardHandler = new ReqMultipleServerForwardHandler(server);
		ReqInnerRpcForwardRelayHandler rpcForwardHandler = new ReqInnerRpcForwardRelayHandler(server);
		registerDirectHandler(authHandler);
		registerDirectHandler(heartbeatHandler);
		registerDirectHandler(forwardHandler);
		registerDirectHandler(multipleServerForwardHandler);
		registerDirectHandler(rpcForwardHandler);
	}

	@Override
	public void connected(Connection connection) {
		super.connected(connection);
		ConnectionRpcAddon rpcAddon = new ConnectionRpcAddon(server.getType(), server.getId(), connection);
		connection.setAttribute(ConnectionRpcAddon.ATTR_KEY, rpcAddon);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		// 默认netty一条链接对应单个线程
		// 内网通信时 改为多线程调用
		int msgId = protocol.getId();
		int seq = protocol.getSeq();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		if (messageHandler != null) {
			Protocol response = messageHandler.handleMessage(protocol);
			if (response != null) {
				response.setSeq(-seq);
				connection.sendProtocol(response);
			}
			return;
		}
		// FIXME 线程池
		CommunicationTaskManager.SERVER.scheduleTask(() -> {
			handleCustomProtocol(connection, protocol);
		});
	}

	private void handleCustomProtocol(Connection connection, Protocol request) {
		int seq = request.getSeq();
		if (seq < 0) {
			// 内网服务器通信 seq负数为rpc返回协议
			ConnectionRpcAddon rpcAddon = connection.getAttribute(ConnectionRpcAddon.ATTR_KEY);
			if (rpcAddon != null) {
				RpcCallbackCache rpcCallbackCache = rpcAddon.getCallbackCache();
				boolean flag = rpcCallbackCache.receiveResponse(-seq, request);
				if (flag) {
					return;
				}
				return;
			}
			// rpc超时的请求 尝试走普通的处理流程
		}
		// 非rpc返回 也不是直接处理的协议
		// 尝试扔到context中处理
		Protocol response = getContext().handleMessage(request);
		if (response != null) {
			response.setSeq(-seq);
			connection.sendProtocol(response);
		}
	}

	protected abstract Context getContext();
}
