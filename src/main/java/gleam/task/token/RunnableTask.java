package gleam.task.token;

import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

public class RunnableTask implements Task, Recoverable {

	private final static ObjectPool<RunnableTask> POOL = new ObjectPool<RunnableTask>() {

		@Override
		protected RunnableTask newObject() {
			return new RunnableTask();
		}
	};

	public static RunnableTask get(Runnable runnable) {
		RunnableTask task = POOL.obtain();
		task.setRunnable(runnable);
		return task;
	}

	private Runnable runnable;

	@Override
	public void recycle() {
		this.runnable = null;

	}

	@Override
	public void execute() throws Exception {
		runnable.run();
		POOL.recycle(this);
	}

	@Override
	public String toDesc() {
		return runnable.getClass().getSimpleName();
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}
