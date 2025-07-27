package gleam.core.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.core.Entity;
import gleam.core.event.GameEvent;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

/**
 * 实体处理事件任务
 * 
 * @author hdh
 *
 */
public class EntityHandleEventTask implements Task, Recoverable {
	private final static Logger logger = LoggerFactory.getLogger(EntityHandleEventTask.class);

	private final static ObjectPool<EntityHandleEventTask> POOL = new ObjectPool<EntityHandleEventTask>() {

		@Override
		protected EntityHandleEventTask newObject() {
			return new EntityHandleEventTask();
		}
	};

	public static EntityHandleEventTask get(Entity<?> entity, GameEvent event) {
		EntityHandleEventTask task = POOL.obtain();
		task.setEntity(entity);
		task.setEvent(event);
		return task;
	}

	private Entity<?> entity;

	private GameEvent event;

	private EntityHandleEventTask() {
	}

	@Override
	public void recycle() {
		this.entity = null;
		this.event = null;
	}

	@Override
	public void execute() throws Exception {
		try {
			entity.handleGameEvent(event);
		} catch (Exception e) {
			logger.error("{}[{}] handle event[{}] error.", entity.getClass().getSimpleName(), entity.getId(),
					event.getClass().getSimpleName(), e);
		}
		POOL.recycle(this);
	}

	public Entity<?> getEntity() {
		return entity;
	}

	public void setEntity(Entity<?> entity) {
		this.entity = entity;
	}

	public GameEvent getEvent() {
		return event;
	}

	public void setEvent(GameEvent event) {
		this.event = event;
	}

	@Override
	public String toDesc() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(":");
		sb.append(entity.getClass().getSimpleName());
		sb.append(entity.getId());
		sb.append("_");
		sb.append(event.getId());
		return sb.toString();
	}
}
