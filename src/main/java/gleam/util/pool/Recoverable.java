package gleam.util.pool;

/**
 * 可回收的对象<br>
 * 用于对象池{@link ObjectPool}
 * 
 * @author hdh
 *
 */
public interface Recoverable {

    void recycle();

}
