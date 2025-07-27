package gleam.communication.inner;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.client.ClientConnectionListener;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.inner.handler.ReqInnerRpcForwardDstHandler;
import gleam.communication.inner.handler.ResInnerAuthenticateHandler;
import gleam.communication.inner.protocol.ReqInnerAuthenticate;
import gleam.communication.rpc.RpcCallbackCache;
import gleam.communication.task.CommunicationTaskManager;
import gleam.core.service.Context;

public abstract class InnerClientConnectionListener<T extends InnerClient> extends ClientConnectionListener<T> {

	protected final RpcCallbackCache rpcCallbackCache;

	public InnerClientConnectionListener(T client) {
		super(client);
		rpcCallbackCache = client.getRpcAddon().getCallbackCache();
	}

	@Override
	public void init() {
		ResInnerAuthenticateHandler authHandler = new ResInnerAuthenticateHandler(client);
		ReqInnerRpcForwardDstHandler rpcForwardHandler = new ReqInnerRpcForwardDstHandler(client, this);
		registerDirectHandler(authHandler);
		registerDirectHandler(rpcForwardHandler);
	}

	@Override
	public void connected(Connection connection) {
		// 客户端连接成功
		// 发送身份认证协议
		client.setConnection(connection);
		logger.info("client[{}] connected.", client.getName());
		sendReqAuth(connection);
	}

	/**
	 * 发送身份认证请求
	 * 
	 * @param connection
	 */
	protected void sendReqAuth(Connection connection) {
		ReqInnerAuthenticate reqAuth = new ReqInnerAuthenticate();
		InnerIdentity localIdentity = client.getLocalIdentity();
		reqAuth.setServerId(localIdentity.getId());
		reqAuth.setServerType(localIdentity.getType().getType());
		connection.sendProtocol(reqAuth);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		// 默认netty一条链接对应单个线程
		// 内网通信时 改为多线程调用
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
		// FIXME 线程池
		CommunicationTaskManager.CLIENT.scheduleTask(() -> {
			handleCustomProtocol(connection, protocol);
		});
	}

	/**
	 * 处理自定义协议
	 * 
	 * @param connection
	 * @param request
	 */
	protected void handleCustomProtocol(Connection connection, Protocol request) {
		int seq = request.getSeq();
		if (seq < 0) {
			// 内网服务器通信 seq负数为rpc返回协议
			boolean flag = rpcCallbackCache.receiveResponse(-seq, request);
			if (flag) {
				return;
			}
			// rpc超时的请求 尝试走普通的处理流程
		}
		// 非rpc返回 也不是直接处理的协议
		// 扔到context中处理
		Protocol response = getContext().handleMessage(request);
		if (response != null) {
			response.setSeq(-seq);
			connection.sendProtocol(response);
		}
	}

	protected abstract Context getContext();

	public RpcCallbackCache getRpcCallbackCache() {
		return rpcCallbackCache;
	}

}
