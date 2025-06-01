package gleam.communication.rpc.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import gleam.communication.Protocol;
import gleam.core.define.BasicErrorCode;
import gleam.exception.ErrorCodeException;
import gleam.util.time.TimeUtil;

/**
 * rpc调用返回
 * 
 * @author hdh
 *
 * @param <T> 返回消息
 */
public class RpcFutureResult<T extends Protocol> extends AbstractRpcCallback<T> {

	private CompletableFuture<T> future = new CompletableFuture<>();

	/**
	 * 错误码
	 */
	private int errorCode;

	public RpcFutureResult() {
	}

	public RpcFutureResult(int seq, long expiredTime) {
		this.seq = seq;
		this.expiredTime = expiredTime;
	}

	public RpcFutureResult(int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public void receiveResponse(T response) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		future.complete(response);
	}

	@Override
	public void receiveReturnCode(int returnCode) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		errorCode = returnCode;
		future.complete(null);
	}

	@Override
	public void handleException(Exception ex) {
		if (!complete.compareAndSet(false, true)) {
			return;
		}
		if (ex instanceof ErrorCodeException ece) {
			errorCode = ece.getErrorCode();
		} else {
			Throwable cause = ex.getCause();
			if (cause != null && cause instanceof ErrorCodeException) {
				ErrorCodeException errorCodeException = (ErrorCodeException) cause;
				errorCode = errorCodeException.getErrorCode();
			} else {
				// 未知错误码
				errorCode = BasicErrorCode.UNKNOW_ERROR;
			}
		}
		future.completeExceptionally(ex);
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public boolean isError() {
		if (!future.isDone()) {
			waitResult();
		}
		return errorCode > 0;
	}

	public int getErrorCode() {
		if (!future.isDone()) {
			waitResult();
		}
		return errorCode;
	}

	public T getData() {
		if (!future.isDone()) {
			waitResult();
		}
		return future.getNow(null);
	}

	/**
	 * 等待结果 直到等待超时<br>
	 */
	private void waitResult() {
		try {
			long waitTime = TimeUtil.now() - expiredTime;
			future.get(waitTime, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			handleException(e);
		}
	}

}
