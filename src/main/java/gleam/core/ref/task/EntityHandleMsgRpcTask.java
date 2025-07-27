package gleam.core.ref.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.rpc.ResponseCallback;
import gleam.core.Entity;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

public class EntityHandleMsgRpcTask implements Task, Recoverable {

	private final static Logger logger = LoggerFactory.getLogger(EntityHandleMsgRpcTask.class);

	private final static ObjectPool<EntityHandleMsgRpcTask> POOL = new ObjectPool<EntityHandleMsgRpcTask>() {

		@Override
		protected EntityHandleMsgRpcTask newObject() {
			return new EntityHandleMsgRpcTask();
		}
	};

	public static EntityHandleMsgRpcTask get(Entity<?> entity, Protocol request, ResponseCallback<?> callback) {
		EntityHandleMsgRpcTask task = POOL.obtain();
		task.setEntity(entity);
		task.setRequest(request);
		task.setCallback(callback);
		return task;
	}

	private Entity<?> entity;

	private Protocol request;

	@SuppressWarnings("rawtypes")
	private ResponseCallback callback;

	private EntityHandleMsgRpcTask() {
	}

	@Override
	public void recycle() {
		this.entity = null;
		this.request = null;
		this.callback = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		try {
			Protocol response = entity.handleMessage(request);
			if (response != null) {
				if (response instanceof ResInnerReturnCode rirc) {
					callback.receiveReturnCode(rirc.getCode());
				} else {
					callback.receiveResponse(response);
				}
			} else {
				callback.receiveReturnCode(0);
			}
		} catch (Exception e) {
			logger.error("refrpc ask[{}] error.", request.getId(), e);
			try {
				callback.handleException(e);
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

	public ResponseCallback<?> getCallback() {
		return callback;
	}

	public void setCallback(ResponseCallback<?> callback) {
		this.callback = callback;
	}

}
