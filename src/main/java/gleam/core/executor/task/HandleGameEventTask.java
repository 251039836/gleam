package gleam.core.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.core.event.GameEvent;
import gleam.core.event.GameEventListener;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

/**
 * 监听器处理事件任务
 * 
 * @author hdh
 *
 */
public class HandleGameEventTask implements Task, Recoverable {
	private final static Logger logger = LoggerFactory.getLogger(HandleGameEventTask.class);

	private final static ObjectPool<HandleGameEventTask> POOL = new ObjectPool<HandleGameEventTask>() {

		@Override
		protected HandleGameEventTask newObject() {
			return new HandleGameEventTask();
		}
	};

	public static HandleGameEventTask get(GameEventListener listener, GameEvent event) {
		HandleGameEventTask task = POOL.get();
		task.setListener(listener);
		task.setEvent(event);
		return task;
	}

	private GameEventListener listener;

	private GameEvent event;

	private HandleGameEventTask() {
	}

	@Override
	public void recycle() {
		this.listener = null;
		this.event = null;
	}

	@Override
	public void execute() throws Exception {
		try {
			listener.handleGameEvent(event);
		} catch (Exception e) {
			logger.error("listener[" + listener.getClass().getSimpleName() + "] handle event["
					+ event.getClass().getName() + "] error.", e);
		}
		POOL.recycle(this);
	}

	public GameEventListener getListener() {
		return listener;
	}

	public void setListener(GameEventListener listener) {
		this.listener = listener;
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
		sb.append(listener.getClass().getSimpleName());
		sb.append("_");
		sb.append(event.getId());
		return sb.toString();
	}
}
