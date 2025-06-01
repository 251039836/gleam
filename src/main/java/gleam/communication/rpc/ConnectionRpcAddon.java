package gleam.communication.rpc;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.inner.protocol.ReqInnerRpcForward;
import gleam.communication.protocol.ProtocolUtil;
import gleam.communication.rpc.impl.RpcCallbackHandler;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.define.BasicErrorCode;
import gleam.exception.RpcInvalidConnectException;
import io.netty.util.AttributeKey;

/**
 * 内网服务器链接的rpc插件<br>
 * 发送的seq>0 返回的seq为发送seq的负值
 * 
 * @author hdh
 *
 */
public class ConnectionRpcAddon {

	public final static AttributeKey<ConnectionRpcAddon> ATTR_KEY = AttributeKey
			.valueOf(ConnectionRpcAddon.class.getSimpleName());

	private final static Logger logger = LoggerFactory.getLogger(ConnectionRpcAddon.class);

	/**
	 * 本地服务器类型
	 */
	private final IdentityType localServerType;
	/**
	 * 本地服务器id
	 */
	private final int localServerId;
	/**
	 * 当前链接
	 */
	private final Connection connection;

	/**
	 * rpc回调缓存
	 */
	private final RpcCallbackCache callbackCache = new RpcCallbackCache();
	/**
	 * 协议序号生成器<br>
	 * ask操作才使用序号
	 */
	private final AtomicInteger seqGenerator = new AtomicInteger();

	public ConnectionRpcAddon(IdentityType localServerType, int localServerId, Connection connection) {
		super();
		this.localServerType = localServerType;
		this.localServerId = localServerId;
		this.connection = connection;
	}

	public void checkExpired() {
		long now = System.currentTimeMillis();
		callbackCache.checkExpired(now);
	}

	public <R extends Protocol> RpcFutureResult<R> askRpc(Protocol request, long timeout) {
		if (!connection.isActive()) {
			logger.error("rpc ask[{}] error. client is not active.", request.getId());
			RpcFutureResult<R> futureResult = new RpcFutureResult<>();
			futureResult.receiveReturnCode(BasicErrorCode.INVALID_CONNECT);
			return futureResult;
		}
		int seq = generateSeq();
		request.setSeq(seq);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcFutureResult<R> futureResult = new RpcFutureResult<>(seq, expiredTime);
		callbackCache.addCallback(futureResult);
		try {
			connection.sendProtocol(request);
		} catch (Exception e) {
			logger.error("rpc ask[{}] error.", request.getId(), e);
			callbackCache.handleException(seq, e);
		}
		return futureResult;
	}

	public <R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback) {
		if (callback == null) {
			connection.sendProtocol(request);
			return;
		}
		if (!connection.isActive()) {
			callback.handleException(new RpcInvalidConnectException());
			return;
		}
		int seq = generateSeq();
		request.setSeq(seq);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcCallbackHandler<?> handler = new RpcCallbackHandler<>(seq, expiredTime, callback);
		callbackCache.addCallback(handler);
		try {
			connection.sendProtocol(request);
		} catch (Exception e) {
			logger.error("rpc ask[{}] error.", request.getId(), e);
			try {
				callbackCache.handleException(seq, e);
			} catch (Exception e2) {
				logger.error("rpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
	}

	public <R extends Protocol> RpcFutureResult<R> forwardAsk(int dstServerType, int dstServerId, Protocol request,
			long timeout) {
		if (!connection.isActive()) {
			logger.error("rpc ask[{}] error. client is not active.", request.getId());
			RpcFutureResult<R> rpcResultFuture = new RpcFutureResult<>();
			rpcResultFuture.receiveReturnCode(BasicErrorCode.INVALID_CONNECT);
			return rpcResultFuture;
		}
		int seq = generateSeq();
		ReqInnerRpcForward protocol = buildRpcForwardProtocol(request, seq, dstServerType, dstServerId);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcFutureResult<R> futureResult = new RpcFutureResult<>(seq, expiredTime);
		callbackCache.addCallback(futureResult);
		try {
			connection.sendProtocol(protocol);
		} catch (Exception e) {
			logger.error("rpc ask[{}] error.", request.getId(), e);
			try {
				callbackCache.handleException(seq, e);
			} catch (Exception e2) {
				logger.error("rpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
		return futureResult;
	}

	public <R extends Protocol> void forwardAsk(int dstServerType, int dstServerId, Protocol request, long timeout,
			ResponseCallback<R> callback) {
		if (callback == null) {
			forwardAsk(dstServerType, dstServerId, request, timeout, callback);
			return;
		}
		if (!connection.isActive()) {
			callback.handleException(new RpcInvalidConnectException());
			return;
		}
		int seq = generateSeq();
		ReqInnerRpcForward protocol = buildRpcForwardProtocol(request, seq, dstServerType, dstServerId);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcCallbackHandler<?> handler = new RpcCallbackHandler<>(seq, expiredTime, callback);
		callbackCache.addCallback(handler);
		try {
			connection.sendProtocol(protocol);
		} catch (Exception e) {
			logger.error("rpc ask[{}] error.", request.getId(), e);
			try {
				callbackCache.handleException(seq, e);
			} catch (Exception e2) {
				logger.error("rpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
	}

	private ReqInnerRpcForward buildRpcForwardProtocol(Protocol forwardMsg, int seq, int dstServerType,
			int dstServerId) {
		forwardMsg.setSeq(seq);
		ReqInnerRpcForward protocol = new ReqInnerRpcForward();
		protocol.setSrcServerType(localServerType.getType());
		protocol.setSrcServerId(localServerId);
		protocol.setDstServerType(dstServerType);
		protocol.setDstServerId(dstServerId);
		protocol.setForwardMsgSeq(seq);
		protocol.setForwardMsgId(forwardMsg.getId());
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(forwardMsg);
		protocol.setForwardMsgData(forwardMsgData);
		return protocol;
	}

	private int generateSeq() {
		int seq = seqGenerator.incrementAndGet();
		if (seq >= 0) {
			return seq;
		}
		// 重置从1开始
		seqGenerator.compareAndSet(seq, 1);
		return seqGenerator.incrementAndGet();
	}

	public Connection getConnection() {
		return connection;
	}

	public RpcCallbackCache getCallbackCache() {
		return callbackCache;
	}

	public AtomicInteger getSeqGenerator() {
		return seqGenerator;
	}

}
