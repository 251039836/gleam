package gleam.communication.rpc.impl;

import gleam.communication.Protocol;
import gleam.communication.rpc.ResponseCallback;

public class RpcCallbackHandler<T extends Protocol> extends AbstractRpcCallback<T> {

	private ResponseCallback<T> callback;

	public RpcCallbackHandler() {
	}

	public RpcCallbackHandler(int seq, long expiredTime, ResponseCallback<T> callback) {
		this.seq = seq;
		this.expiredTime = expiredTime;
		this.callback = callback;
	}

	@Override
	public void receiveResponse(T response) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		callback.receiveResponse(response);
	}

	@Override
	public void receiveReturnCode(int returnCode) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		callback.receiveReturnCode(returnCode);
	}

	@Override
	public void handleException(Exception ex) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		callback.handleException(ex);
	}

	public ResponseCallback<T> getCallback() {
		return callback;
	}

	public void setCallback(ResponseCallback<T> callback) {
		this.callback = callback;
	}

}
