package gleam.communication.inner;

import java.net.InetSocketAddress;

import gleam.communication.Protocol;
import gleam.communication.client.ClientConnectionListener;
import gleam.communication.client.impl.SocketClient;
import gleam.communication.define.RpcConstant;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.rpc.ConnectionRpcAddon;
import gleam.communication.rpc.ResponseCallback;
import gleam.communication.rpc.RpcEndpoint;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.communication.server.impl.SocketChannelInitializer;
import gleam.communication.task.CommunicationTaskManager;
import gleam.task.TaskHandle;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 内网socket长连接客户端<br>
 * 用于服务器之间连接<br>
 * 支持rpc调用
 * 
 * @author hdh
 *
 * @param <T>
 */
public abstract class InnerClient extends SocketClient implements RpcEndpoint {

	/**
	 * 定时任务 检查并清理过期回调
	 */
	protected TaskHandle callbackTimerTask;

	protected ConnectionRpcAddon rpcAddon;

	public InnerClient(InnerIdentity localIdentity, InnerIdentity remoteIdentity, InetSocketAddress remoteAddress) {
		super(localIdentity, remoteIdentity, remoteAddress);
	}

	@Override
	protected ChannelInitializer<SocketChannel> buildChannelInitializer(
			ClientConnectionListener<?> connectionListener) {
		SocketChannelInitializer initializer = new SocketChannelInitializer(connectionListener);
		return initializer;
	}

	@Override
	public void onStart() {
		connector.connect();
		// 心跳/断线重连检查
		startTickTask();
		super.onStart();
		startCallbackTimerTask();
	}

	@Override
	public void onStop() {
		cancelCallbackTimerTask();
		super.onStop();
	}

	protected void cancelCallbackTimerTask() {
		if (callbackTimerTask != null) {
			callbackTimerTask.cancel();
		}
		callbackTimerTask = null;
	}

	/**
	 * 启动回调过期检查定时器
	 */
	protected void startCallbackTimerTask() {
		cancelCallbackTimerTask();
		callbackTimerTask = CommunicationTaskManager.TIMER.scheduleTask(() -> {
			rpcAddon.checkExpired();
		}, 0, RpcConstant.CALLBACK_EXPIRED_CHECK_INTERVAL);
	}

//	@Override
//	public <R extends Protocol> CompletableFuture<R> ask(Protocol request) {
//		return rpcAddon.ask(request, RpcConstant.DEFAULT_RPC_TIMEOUT);
//	}
//
//	@Override
//	public <R extends Protocol> CompletableFuture<R> ask(Protocol request, long timeout) {
//		return rpcAddon.ask(request, timeout);
//	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> ask(Protocol request) {
		return rpcAddon.askRpc(request, RpcConstant.DEFAULT_RPC_TIMEOUT);
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout) {
		return rpcAddon.askRpc(request, timeout);
	}

	@Override
	public <R extends Protocol> void ask(Protocol request, ResponseCallback<R> callback) {
		rpcAddon.ask(request, RpcConstant.DEFAULT_RPC_TIMEOUT, callback);
	}

	@Override
	public <R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback) {
		rpcAddon.ask(request, timeout, callback);
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> forwardAsk(int dstServerType, int dstServerId, Protocol request) {
		return rpcAddon.forwardAsk(dstServerType, dstServerId, request, RpcConstant.DEFAULT_RPC_TIMEOUT);
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> forwardAsk(int dstServerType, int dstServerId, Protocol request,
			long timeout) {
		return rpcAddon.forwardAsk(dstServerType, dstServerId, request, timeout);
	}

	@Override
	public <R extends Protocol> void forwardAsk(int dstServerType, int dstServerId, Protocol request,
			ResponseCallback<R> callback) {
		rpcAddon.forwardAsk(dstServerType, dstServerId, request, RpcConstant.DEFAULT_RPC_TIMEOUT, callback);
	}

	@Override
	public <R extends Protocol> void forwardAsk(int dstServerType, int dstServerId, Protocol request, long timeout,
			ResponseCallback<R> callback) {
		rpcAddon.forwardAsk(dstServerType, dstServerId, request, timeout, callback);
	}

	public ConnectionRpcAddon getRpcAddon() {
		return rpcAddon;
	}

}
