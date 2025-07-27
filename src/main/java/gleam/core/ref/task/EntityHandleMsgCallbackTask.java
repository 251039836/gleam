package gleam.core.ref.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.Entity;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

public class EntityHandleMsgCallbackTask implements Task, Recoverable {

	private final static Logger logger = LoggerFactory.getLogger(EntityHandleMsgCallbackTask.class);

	private final static ObjectPool<EntityHandleMsgCallbackTask> POOL = new ObjectPool<EntityHandleMsgCallbackTask>() {

		@Override
		protected EntityHandleMsgCallbackTask newObject() {
			return new EntityHandleMsgCallbackTask();
		}
	};

	public static EntityHandleMsgCallbackTask get(Entity<?> entity, Protocol request, RpcFutureResult<?> futureResult) {
		EntityHandleMsgCallbackTask task = POOL.obtain();
		task.setEntity(entity);
		task.setRequest(request);
		task.setFutureResult(futureResult);
		return task;
	}

	private Entity<?> entity;

	private Protocol request;

	@SuppressWarnings("rawtypes")
	private RpcFutureResult futureResult;

	private EntityHandleMsgCallbackTask() {
	}

	@Override
	public void recycle() {
		this.entity = null;
		this.request = null;
		this.futureResult = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		try {
			Protocol response = entity.handleMessage(request);
			if (response != null) {
				if (response instanceof ResInnerReturnCode rirc) {
					futureResult.receiveReturnCode(rirc.getCode());
				} else {
					futureResult.receiveResponse(response);
				}
			} else {
				futureResult.receiveReturnCode(0);
			}
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				futureResult.handleException(e);
			} catch (Exception e2) {
				logger.error("refrpc ask[{}] handleException error.", request.getId(), e2);
			}
		}
		POOL.recycle(this);
	}

	public Entity<?> getEntity() {
		return entity;
	}

	public void setEntity(Entity<?> entity) {
		this.entity = entity;
	}

	public Protocol getRequest() {
		return request;
	}

	public void setRequest(Protocol request) {
		this.request = request;
	}

	public RpcFutureResult<?> getFutureResult() {
		return futureResult;
	}

	public void setFutureResult(RpcFutureResult<?> futureResult) {
		this.futureResult = futureResult;
	}

	@Override
	public String toDesc() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(":");
		sb.append(entity.getClass().getSimpleName());
		sb.append(entity.getId());
		sb.append("_");
		sb.append(request.getId());
		return sb.toString();
	}

}
