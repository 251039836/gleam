package gleam.util.pool;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 对象池
 * 
 * @author hdh
 *
 * @param <T> 需有无参构造函数
 */
public abstract class ObjectPool<T extends Recoverable> {

    protected final static int DEFAULT_MAX_SIZE = 2000;

    private final Queue<T> queue = buildQueue();

    /**
     * {@link ConcurrentLinkedQueue}无法设置长度 size方法需要遍历整个队列 性能较差<br>
     * {@link ArrayBlockingQueue}只有1个锁 即同一时间只有1个线程能添加或移除<br>
     * {@link LinkedBlockingQueue}存取分别有1个锁 则同一时间 可以1个线程添加 1个线程移除
     * 
     * @return
     */
    protected Queue<T> buildQueue() {
        return new LinkedBlockingQueue<>(DEFAULT_MAX_SIZE);
    }

    /**
     * 从对象池中获取1个对象<br>
     * 若为空 则新建1个对象
     * 
     * @return
     */
    public T get() {
        T obj = queue.poll();
        if (obj == null) {
            obj = newObject();
        }
        return obj;
    }

    /**
     * 池的大小
     * 
     * @return
     */
    protected int getMaxSize() {
        return DEFAULT_MAX_SIZE;
    }

    public Queue<T> getQueue() {
        return queue;
    }

    protected abstract T newObject();

    /**
     * 回收对象
     * 
     * @param obj
     */
    public void recycle(T obj) {
        obj.recycle();
        queue.offer(obj);
    }
}
