package gleam.communication.rpc;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.exception.RpcShutdownException;
import gleam.exception.RpcTimeoutException;

/**
 * rpc回调缓存
 * 
 * @author hdh
 *
 * @param <T>
 */
public class RpcCallbackCache {

	private final static Logger logger = LoggerFactory.getLogger(RpcCallbackCache.class);

	private final ConcurrentMap<Integer, RpcCallback<? extends Protocol>> callbackMap = new ConcurrentHashMap<>();

	public void addCallback(RpcCallback<? extends Protocol> callback) {
		callbackMap.put(callback.getSeq(), callback);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean receiveResponse(int seq, Protocol response) {
		RpcCallback callback = callbackMap.remove(seq);
		if (callback == null) {
			logger.warn("receive response[{}],but callback is expired.", response.getId());
			if (response.getId() == ResInnerReturnCode.ID) {
				// 使用通用返回码的协议 本身无意义
				return true;
			}
			return false;
		}
		try {
			if (response instanceof ResInnerReturnCode returnCodeResponse) {
				int code = returnCodeResponse.getCode();
				callback.receiveReturnCode(code);
			} else {
				callback.receiveResponse(response);
			}
		} catch (Exception e) {
			logger.error("callback receiveResponse[{}] error.", response.getId(), e);
		}
		return true;
	}

	public void handleException(int seq, Exception ex) {
		RpcCallback<?> callback = callbackMap.remove(seq);
		if (callback == null) {
			return;
		}
		try {
			callback.handleException(ex);
		} catch (Exception e) {
			logger.error("callback handleException error.", e);
		}

	}

	/**
	 * 检查并清理过期回调
	 * 
	 * @param now
	 */
	public void checkExpired(long now) {
		if (callbackMap.isEmpty()) {
			return;
		}
		Iterator<Entry<Integer, RpcCallback<? extends Protocol>>> iterator = callbackMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, RpcCallback<? extends Protocol>> entry = iterator.next();
			RpcCallback<? extends Protocol> callback = entry.getValue();
			if (!callback.isTimeout(now)) {
				continue;
			}
			try {
				callback.handleException(new RpcTimeoutException());
			} catch (Exception e) {
				logger.error("callback handle timeout error.", e);
			}
			iterator.remove();
		}
	}

	public void shutdown() {
		if (callbackMap.isEmpty()) {
			return;
		}
		Iterator<Entry<Integer, RpcCallback<? extends Protocol>>> iterator = callbackMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, RpcCallback<? extends Protocol>> entry = iterator.next();
			RpcCallback<? extends Protocol> callback = entry.getValue();
			try {
				callback.handleException(new RpcShutdownException());
			} catch (Exception e) {
				logger.error("callback handle shutdown error.", e);
			}
			iterator.remove();
		}

	}

	public RpcCallback<?> getCallback(int seq) {
		return callbackMap.get(seq);
	}

	public ConcurrentMap<Integer, RpcCallback<? extends Protocol>> getCallbackMap() {
		return callbackMap;
	}

}
