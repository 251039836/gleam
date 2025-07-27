package gleam.task.token;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gleam.task.Task;
import gleam.util.ThreadFactoryImpl;

public class DefaultTokenTaskQueueExecutor implements TokenTaskQueueExecutor {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final static int DEFAULT_QUEUE_MAX_SIZE = 16;

	protected final static int CACHE_EXPIRED = 30;
	/**
	 * 标记,任务队列
	 */
	protected final Cache<Long, TokenTaskQueue> tokenQueues;

	protected final ScheduledExecutorService executor;

	public DefaultTokenTaskQueueExecutor(String threadPrefix) {
		ThreadFactory threadFactory = new ThreadFactoryImpl(threadPrefix);
		int cpuNum = Runtime.getRuntime().availableProcessors();
		this.executor = Executors.newScheduledThreadPool(cpuNum + 1, threadFactory);
		this.tokenQueues = newQueueCache();
	}

	public DefaultTokenTaskQueueExecutor(String threadPrefix, int poolSize) {
		ThreadFactory threadFactory = new ThreadFactoryImpl(threadPrefix);
		this.executor = Executors.newScheduledThreadPool(poolSize, threadFactory);
		this.tokenQueues = newQueueCache();
	}

	protected TokenTaskQueue getQueue(long token) {
		try {
			TokenTaskQueue queue = tokenQueues.get(token, () -> {
				return newQueue(token);
			});
			return queue;
		} catch (ExecutionException e) {
			logger.error("getQueue[{}] error.", token, e);
		}
		return newQueue(token);
	}

	protected TokenTaskQueue newQueue(long token) {
		// 队列有默认大小 超出无法处理时 抛弃新任务
		return new TokenTaskQueue(token, new LinkedBlockingQueue<>(DEFAULT_QUEUE_MAX_SIZE));
	}

	protected Cache<Long, TokenTaskQueue> newQueueCache() {
		return CacheBuilder.newBuilder()//
				.expireAfterAccess(CACHE_EXPIRED, TimeUnit.MINUTES) // 指定时间内未读写过时移除
				.build();
	}

	@Override
	public <V> Future<V> submit(long token, Callable<V> callable) {
		DefaultFutureTask<V> task = new DefaultFutureTask<>(callable);
		TokenTaskQueue tokenQueue = getQueue(token);
		// 判断是否当前线程正在执行该任务队列
		Thread runThread = tokenQueue.getRunThread();
		Thread curThread = Thread.currentThread();
		if (curThread.equals(runThread)) {
			// 当前线程正在执行该任务队列
			// 直接执行该任务 不再扔进任务队列中
			// 避免队列前面的任务阻塞等待队列后面的任务完成
			task.execute();
		} else {
			tokenQueue.addTask(task);
			if (!tokenQueue.isRunning()) {
				executor.submit(tokenQueue);
			}
		}
		return task;
	}

	@Override
	public void submit(long token, Task task) {
		TokenTaskQueue tokenQueue = getQueue(token);
		tokenQueue.addTask(task);
		if (!tokenQueue.isRunning()) {
			executor.submit(tokenQueue);
		}
	}

	@Override
	public Executor getTokenExecutor(long token) {
		return new TokenExecutor(token, this);
	}

	public Cache<Long, TokenTaskQueue> getTokenQueues() {
		return tokenQueues;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	private static class TokenExecutor implements Executor {
		private long token;

		private TokenTaskQueueExecutor queueExecutor;

		private TokenExecutor(long token, TokenTaskQueueExecutor queueExecutor) {
			super();
			this.token = token;
			this.queueExecutor = queueExecutor;
		}

		@Override
		public void execute(Runnable command) {
			RunnableTask task = RunnableTask.get(command);
			queueExecutor.submit(token, task);
		}

	}

}
