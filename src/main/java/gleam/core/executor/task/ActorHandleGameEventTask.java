package gleam.core.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.core.actor.Actor;
import gleam.core.event.GameEvent;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

/**
 * actor处理事件任务<br>
 * {@link Actor#handleGameEvent(GameEvent)}
 * 
 * @author hdh
 *
 */
public class ActorHandleGameEventTask implements Task, Recoverable {
	private final static Logger logger = LoggerFactory.getLogger(ActorHandleGameEventTask.class);

	private final static ObjectPool<ActorHandleGameEventTask> POOL = new ObjectPool<ActorHandleGameEventTask>() {

		@Override
		protected ActorHandleGameEventTask newObject() {
			return new ActorHandleGameEventTask();
		}
	};

	public static ActorHandleGameEventTask get(Actor<?> actor, GameEvent event) {
		ActorHandleGameEventTask task = POOL.get();
		task.setActor(actor);
		task.setEvent(event);
		return task;
	}

	private Actor<?> actor;

	private GameEvent event;

	private ActorHandleGameEventTask() {
	}

	@Override
	public void recycle() {
		this.actor = null;
		this.event = null;
	}

	@Override
	public void execute() throws Exception {
		try {
			actor.handleGameEvent(event);
		} catch (Exception e) {
			logger.error("actor[{}] handle event[{}] error.", actor.getId(), event.getClass().getName(), e);
		}
		POOL.recycle(this);
	}

	public Actor<?> getActor() {
		return actor;
	}

	public void setActor(Actor<?> actor) {
		this.actor = actor;
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
		sb.append(actor.getId());
		sb.append("_");
		sb.append(event.getId());
		return sb.toString();
	}
}
