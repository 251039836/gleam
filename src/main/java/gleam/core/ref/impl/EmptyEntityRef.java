package gleam.core.ref.impl;

import gleam.communication.Protocol;
import gleam.communication.rpc.ResponseCallback;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.ref.EntityRef;

public class EmptyEntityRef implements EntityRef {

	/**
	 * 实体不存在的错误码
	 */
	private int notExistErrorCode;

	public EmptyEntityRef(int notExistErrorCode) {
		super();
		this.notExistErrorCode = notExistErrorCode;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public int getEntityType() {
		return 0;
	}

	@Override
	public void tell(Protocol message) {
		// do nothing
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout) {
		RpcFutureResult<R> result = new RpcFutureResult<>(-request.getSeq(), timeout);
		result.receiveReturnCode(notExistErrorCode);
		return result;
	}

	@Override
	public <R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback) {
		callback.receiveReturnCode(notExistErrorCode);
	}

}
