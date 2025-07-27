package gleam.core.executor;

import java.util.Collection;

import gleam.communication.Protocol;
import gleam.core.actor.Actor;
import gleam.core.event.GameEvent;
import gleam.core.executor.task.EntityHandleEventTask;
import gleam.core.executor.task.EntityHandleMsgTask;
import gleam.task.Task;
import gleam.task.token.DefaultTokenTaskQueueExecutor;

/**
 * actor任务执行器
 * 
 * @author hdh
 *
 */
public class ActorTaskExecutor extends DefaultTokenTaskQueueExecutor {

	private static ActorTaskExecutor instance = new ActorTaskExecutor();

	public static ActorTaskExecutor getInstance() {
		return instance;
	}

	public ActorTaskExecutor() {
		super("actor");
	}

	public void handleProtocol(Actor<?> actor, Protocol request) {
		long actorId = actor.getId();
		EntityHandleMsgTask task = EntityHandleMsgTask.get(actor, request);
		submit(actorId, task);
	}

	public void handleGameEvent(Actor<?> actor, GameEvent event) {
		long actorId = actor.getId();
		EntityHandleEventTask task = EntityHandleEventTask.get(actor, event);
		submit(actorId, task);
	}

	public void handleTask(Actor<?> actor, Task task) {
		long actorId = actor.getId();
		submit(actorId, task);
	}

	/**
	 * 发送事件给所有actor
	 * 
	 * @param actors
	 * @param event
	 */
	public void fireGameEvent(Collection<? extends Actor<?>> actors, GameEvent event) {
		if (actors == null || actors.isEmpty()) {
			return;
		}
		for (Actor<?> actor : actors) {
			long playerId = actor.getId();
			EntityHandleEventTask task = EntityHandleEventTask.get(actor, event);
			submit(playerId, task);
		}
	}

}
