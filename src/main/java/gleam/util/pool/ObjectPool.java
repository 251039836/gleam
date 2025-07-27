package gleam.util.pool;

import java.util.ArrayList;
import java.util.List;
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

	private final static List<ObjectPool<?>> ALL_OBJECT_POOLS = new ArrayList<>();

	protected final static int DEFAULT_MAX_SIZE = 1000;
	/**
	 * 创建对象个数
	 */
	protected volatile long newCount = 0;
	/**
	 * 获取对象个数
	 */
	protected volatile long obtainCount = 0;
	/**
	 * 回收对象个数
	 */
	protected volatile long recycleCount = 0;

	protected final Queue<T> queue;

	protected ObjectPool() {
		this(DEFAULT_MAX_SIZE);
	}

	protected ObjectPool(int maxSize) {
		queue = buildQueue(maxSize);
		ObjectPool.ALL_OBJECT_POOLS.add(this);
	}

	/**
	 * {@link ConcurrentLinkedQueue}无法设置长度 size方法需要遍历整个队列 性能较差<br>
	 * {@link ArrayBlockingQueue}只有1个锁 即同一时间只有1个线程能添加或移除<br>
	 * {@link LinkedBlockingQueue}存取分别有1个锁 则同一时间 可以1个线程添加 1个线程移除
	 * 
	 * @param maxSize
	 * @return
	 */
	protected Queue<T> buildQueue(int maxSize) {
		return new LinkedBlockingQueue<>(maxSize);
	}

	/**
	 * 从对象池中获取1个对象<br>
	 * 若为空 则新建1个对象
	 * 
	 * @return
	 */
	public T obtain() {
		T obj = queue.poll();
		if (obj == null) {
			obj = newObject();
			newCount++;
		}
		obtainCount++;
		return obj;
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
		boolean add = queue.offer(obj);
		if (add) {
			recycleCount++;
		}
	}

	/**
	 * 打印对象池统计数据
	 * 
	 * @return
	 */
	public static String printStatistics() {
		StringBuffer sb = new StringBuffer();
		for (ObjectPool<?> pool : ALL_OBJECT_POOLS) {
			sb.append(pool.getClass().getName());
			sb.append(" 池内:").append(pool.queue.size());
			sb.append(" 获取:").append(pool.obtainCount);
			sb.append(" 创建:").append(pool.newCount);
			sb.append(" 回收:").append(pool.recycleCount);
			sb.append("\r\n");
		}
		return sb.toString();
	}

}
