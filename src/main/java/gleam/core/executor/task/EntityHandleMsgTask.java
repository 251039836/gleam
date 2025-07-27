package gleam.core.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.core.Entity;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

/**
 * 实体处理消息任务<br>
 * 返回消息使用{@link Protocol#getConnection()}返回
 * 
 * @author hdh
 *
 */
public class EntityHandleMsgTask implements Task, Recoverable {

	private final static Logger logger = LoggerFactory.getLogger(EntityHandleMsgTask.class);

	private final static ObjectPool<EntityHandleMsgTask> POOL = new ObjectPool<EntityHandleMsgTask>() {

		@Override
		protected EntityHandleMsgTask newObject() {
			return new EntityHandleMsgTask();
		}
	};

	public static EntityHandleMsgTask get(Entity<?> entity, Protocol request) {
		EntityHandleMsgTask task = POOL.obtain();
		task.setEntity(entity);
		task.setRequest(request);
		return task;
	}

	private Entity<?> entity;

	private Protocol request;

	private EntityHandleMsgTask() {
	}

	@Override
	public void recycle() {
		this.entity = null;
		this.request = null;
	}

	@Override
	public void execute() throws Exception {
		Protocol response = null;
		try {
			response = entity.handleMessage(request);
		} catch (Exception e) {
			logger.error("{}[{}] handle msg[{}] error.", entity.getClass().getSimpleName(), entity.getId(),
					request.getId(), e);
		}
		if (response != null) {
			Connection connection = request.getConnection();
			if (connection != null) {
				response.setSeq(-request.getSeq());
				connection.sendProtocol(response);
			} else {
				logger.warn("{}[{}] handle msg[{}] response[{}],but connection not exists.",
						entity.getClass().getSimpleName(), entity.getId(), request.getId(), response.getId());
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
}
