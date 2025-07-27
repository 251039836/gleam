package gleam.core.ref.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.protocol.ProtocolUtil;
import gleam.communication.rpc.ResponseCallback;
import gleam.communication.rpc.RpcCallback;
import gleam.communication.rpc.RpcCallbackCache;
import gleam.communication.rpc.impl.RpcCallbackHandler;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.define.BasicErrorCode;
import gleam.core.ref.EntityAddress;
import gleam.core.ref.EntityRef;
import gleam.core.ref.EntityRefManager;
import gleam.core.ref.define.EntityRefState;
import gleam.core.ref.protocol.ReqEntityForward;
import gleam.core.service.Context;
import gleam.util.time.TimeUtil;

/**
 * 其他进程的实体在当前进程中的引用
 * 
 * @author hdh
 */
public class RemoteEntityRef implements EntityRef {

	private final static Logger logger = LoggerFactory.getLogger(RemoteEntityRef.class);

	private long id;

	private int type;
	// -------------------------------------------------------------------------
	private final AtomicInteger state = new AtomicInteger();
	/**
	 * 实例所在地址
	 */
	private EntityAddress address;
	/**
	 * 创建时间
	 */
	private long createTime;
	/**
	 * 最后次操作时间
	 */
	private long lastTime;
	// -------------------------------------------------------------------------
	/**
	 * 等待发送的消息
	 */
	private Queue<Protocol> waitSendMsgs = new LinkedBlockingQueue<>();

	/**
	 * 协议序号生成器<br>
	 * ask操作才使用序号
	 */
	private final AtomicInteger seqGenerator = new AtomicInteger();

	private RpcCallbackCache callbackCache = new RpcCallbackCache();

	@Override
	public void tell(Protocol message) {
		if (!isActive()) {
			waitSendMsgs.add(message);
			return;
		}
		sendProtocol(message);
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout) {
		if (state.get() == EntityRefState.INVALID) {
			RpcFutureResult<R> result = new RpcFutureResult<>();
			result.receiveReturnCode(BasicErrorCode.ENTITY_NOT_EXISTS);
			return result;
		}
		int seq = generateSeq();
		request.setSeq(seq);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcFutureResult<R> result = new RpcFutureResult<>(seq, expiredTime);
		callbackCache.addCallback(result);
		if (!isActive()) {
			waitSendMsgs.add(request);
			return result;
		}
		try {
			sendProtocol(request);
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				callbackCache.handleException(seq, e);
			} catch (Exception e2) {
				logger.error("refrpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
		return result;
	}

	@Override
	public <R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback) {
		if (state.get() == EntityRefState.INVALID) {
			callback.receiveReturnCode(BasicErrorCode.ENTITY_NOT_EXISTS);
			return;
		}
		int seq = generateSeq();
		request.setSeq(seq);
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcCallbackHandler<?> handler = new RpcCallbackHandler<>(seq, expiredTime, callback);
		callbackCache.addCallback(handler);
		if (!isActive()) {
			waitSendMsgs.add(request);
			return;
		}
		try {
			sendProtocol(request);
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				callbackCache.handleException(seq, e);
			} catch (Exception e2) {
				logger.error("refrpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
	}

	private boolean isActive() {
		return state.get() == EntityRefState.RUN;
	}

	private void sendProtocol(Protocol forwardMsg) {
		int dstServerType = address.getType().getType();
		int dstServerId = address.getId();
		Connection connection = EntityRefManager.getInstance().getServerConnection(dstServerType, dstServerId);
		if (connection == null || !connection.isActive()) {
			return;
		}
		Context context = EntityRefManager.getInstance().getOwner();
		ReqEntityForward protocol = new ReqEntityForward();
		protocol.setSrcServerType(context.getServerType().getType());
		protocol.setSrcServerId(context.getServerId());
		protocol.setDstServerType(dstServerType);
		protocol.setDstServerId(dstServerId);
		protocol.setDstEntityType(type);
		protocol.setDstEntityId(id);
		protocol.setForwardMsgId(forwardMsg.getId());
		protocol.setForwardMsgSeq(forwardMsg.getSeq());
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(forwardMsg);
		protocol.setForwardMsgData(forwardMsgData);
		connection.sendProtocol(protocol);
	}

	public EntityAddress getAddress() {
		return address;
	}

	public AtomicInteger getState() {
		return state;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setAddress(EntityAddress address) {
		this.address = address;
	}

	@Override
	public int getEntityType() {
		return type;
	}

	@Override
	public long getId() {
		return id;
	}

	public void setState(int state) {
		this.state.set(state);
	}

	public RpcCallbackCache getCallbackCache() {
		return callbackCache;
	}

	public void setCallbackCache(RpcCallbackCache callbackCache) {
		this.callbackCache = callbackCache;
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

	/**
	 * 处理检测实体是否存在结果
	 * 
	 * @param entityExists
	 */
	public void handleCheckResult(boolean entityExists) {
		lastTime = TimeUtil.now();
		if (!entityExists) {
			state.set(EntityRefState.INVALID);
			waitSendMsgs.clear();
			ConcurrentMap<Integer, RpcCallback<? extends Protocol>> callbackMap = callbackCache.getCallbackMap();
			for (RpcCallback<?> callback : callbackMap.values()) {
				callback.receiveReturnCode(BasicErrorCode.ENTITY_NOT_EXISTS);
			}
			callbackMap.clear();
			return;
		}
		state.set(EntityRefState.RUN);
		if (waitSendMsgs.isEmpty()) {
			return;
		}
		int dstServerType = address.getType().getType();
		int dstServerId = address.getId();
		Connection connection = EntityRefManager.getInstance().getServerConnection(dstServerType, dstServerId);
		if (connection == null || !connection.isActive()) {
			waitSendMsgs.clear();
			return;
		}
		Context context = EntityRefManager.getInstance().getOwner();
		int srcServerType = context.getServerType().getType();
		int srcServerId = context.getServerId();
		while (!waitSendMsgs.isEmpty()) {
			Protocol forwardMsg = waitSendMsgs.peek();
			int seq = forwardMsg.getSeq();
			if (seq != 0) {
				RpcCallback<?> callback = callbackCache.getCallback(-seq);
				if (callback == null) {
					// 已过期
					continue;
				}
			}
			ReqEntityForward protocol = new ReqEntityForward();
			protocol.setSrcServerType(srcServerType);
			protocol.setSrcServerId(srcServerId);
			protocol.setDstServerType(dstServerType);
			protocol.setDstServerId(dstServerId);
			protocol.setDstEntityType(type);
			protocol.setDstEntityId(id);
			protocol.setForwardMsgId(forwardMsg.getId());
			protocol.setForwardMsgSeq(forwardMsg.getSeq());
			byte[] forwardMsgData = ProtocolUtil.encodeMessage(forwardMsg);
			protocol.setForwardMsgData(forwardMsgData);
			connection.sendProtocol(protocol);

		}
	}
}
