package gleam.core.actor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import gleam.core.AbstractEntity;
import gleam.core.Component;
import gleam.core.event.GameEvent;
import gleam.core.executor.ActorTaskExecutor;
import gleam.core.executor.task.EntityHandleEventTask;
import gleam.task.Task;

public abstract class AbstractActor<C extends Component> extends AbstractEntity<C> implements Actor<C> {

	@Override
	public <V> Future<V> submitCallback(long token, Callable<V> callable) {
		return ActorTaskExecutor.getInstance().submit(getId(), callable);
	}

	@Override
	public void submitTask(Task task) {
		ActorTaskExecutor.getInstance().submit(getId(), task);
	}

	@Override
	public void submitHandleEvent(GameEvent event) {
		EntityHandleEventTask task = EntityHandleEventTask.get(this, event);
		ActorTaskExecutor.getInstance().submit(getId(), task);
	}
}
