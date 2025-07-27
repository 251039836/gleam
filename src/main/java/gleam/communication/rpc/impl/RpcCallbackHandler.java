package gleam.communication.rpc.impl;

import java.util.concurrent.Executor;

import gleam.communication.Protocol;
import gleam.communication.rpc.ResponseCallback;
import gleam.task.token.TokenTaskQueueExecutor;

public class RpcCallbackHandler<T extends Protocol> extends AbstractRpcCallback<T> {

	private ResponseCallback<T> callback;

	private Executor executor;

	public RpcCallbackHandler() {
	}

	public RpcCallbackHandler(int seq, long expiredTime, ResponseCallback<T> callback) {
		this.seq = seq;
		this.expiredTime = expiredTime;
		this.callback = callback;
	}

	public RpcCallbackHandler(int seq, long expiredTime, ResponseCallback<T> callback, Executor executor) {
		this.seq = seq;
		this.expiredTime = expiredTime;
		this.callback = callback;
		this.executor = executor;
	}

	public RpcCallbackHandler(int seq, long expiredTime, ResponseCallback<T> callback,
			TokenTaskQueueExecutor queueExecutor, long token) {
		this.seq = seq;
		this.expiredTime = expiredTime;
		this.callback = callback;
		this.executor = queueExecutor.getTokenExecutor(token);
	}

	@Override
	public void receiveResponse(T response) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		if (executor != null) {
			executor.execute(() -> {
				callback.receiveResponse(response);
			});
		} else {
			callback.receiveResponse(response);
		}
	}

	@Override
	public void receiveReturnCode(int returnCode) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		if (executor != null) {
			executor.execute(() -> {
				callback.receiveReturnCode(returnCode);
			});
		} else {
			callback.receiveReturnCode(returnCode);
		}
	}

	@Override
	public void handleException(Exception ex) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		if (executor != null) {
			executor.execute(() -> {
				callback.handleException(ex);
			});
		} else {
			callback.handleException(ex);
		}
	}

	public ResponseCallback<T> getCallback() {
		return callback;
	}

	public void setCallback(ResponseCallback<T> callback) {
		this.callback = callback;
	}

}
