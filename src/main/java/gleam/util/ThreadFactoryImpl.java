package gleam.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import gleam.exception.handler.DefaultUncaughtExceptionHandler;

/**
 * 自定义前缀的线程组<br>
 * 只是默认线程组自定义前缀<br>
 * 使用自定义的未捕获错误处理类<br>
 * {@link Executors#DefaultThreadFactory}
 * {@link DefaultUncaughtExceptionHandler}
 * 
 * @author hdh
 *
 */
public class ThreadFactoryImpl implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private final ThreadGroup group;

    @SuppressWarnings("removal")
	public ThreadFactoryImpl(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public ThreadGroup getGroup() {
        return group;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public AtomicInteger getThreadNumber() {
        return threadNumber;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setUncaughtExceptionHandler(DefaultUncaughtExceptionHandler.getInstance());
        return thread;
    }
}
