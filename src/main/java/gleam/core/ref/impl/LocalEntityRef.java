package gleam.core.ref.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;
import gleam.communication.rpc.ResponseCallback;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.Entity;
import gleam.core.executor.task.EntityHandleMsgTask;
import gleam.core.ref.EntityRef;
import gleam.core.ref.task.EntityHandleMsgCallbackTask;
import gleam.core.ref.task.EntityHandleMsgRpcTask;

public class LocalEntityRef implements EntityRef {

	private final static Logger logger = LoggerFactory.getLogger(LocalEntityRef.class);
	private long id;

	private int type;

	private Entity<?> entity;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public int getEntityType() {
		return type;
	}

	@Override
	public void tell(Protocol message) {
		EntityHandleMsgTask task = EntityHandleMsgTask.get(entity, message);
		entity.submitTask(task);
	}

	@Override
	public <R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout) {
		int seq = request.getSeq();
		long now = System.currentTimeMillis();
		long expiredTime = now + timeout;
		RpcFutureResult<R> futureResult = new RpcFutureResult<>(seq, expiredTime);
		try {
			EntityHandleMsgCallbackTask task = EntityHandleMsgCallbackTask.get(entity, request, futureResult);
			entity.submitTask(task);
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				futureResult.handleException(e);
			} catch (Exception e2) {
				logger.error("refrpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
		return futureResult;
	}

	@Override
	public <R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback) {
		try {
			EntityHandleMsgRpcTask task = EntityHandleMsgRpcTask.get(entity, request, callback);
			entity.submitTask(task);
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				callback.handleException(e);
			} catch (Exception e2) {
				logger.error("refrpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Entity<?> getEntity() {
		return entity;
	}

	public void setEntity(Entity<?> entity) {
		this.entity = entity;
	}

	public void setId(long id) {
		this.id = id;
	}

}
