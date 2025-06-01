package gleam.persistence;

import java.util.Collection;
import java.util.List;

/**
 * 数据保存工具<br>
 * 提供阻塞/异步保存数据的方法<br>
 * 若数据继承了{@link Changeable}<br>
 * 堵塞保存时 保存成功才设置为无变化<br>
 * 异步保存时 放进saver中就设为无变化
 * 
 * 
 * @author hdh
 *
 * @param <T> 数据 未必是PersistenceData的子类 可能只是个普通的bean
 */
public interface Saver<T> {

    void startup();

    void shutdown();

    /**
     * 异步保存数据
     * 
     * @param bean
     */
    void asyncSave(T bean);

    /**
     * 批量保存数据
     * 
     * @param beans
     * @param block 是否阻塞保存
     * @throws Exception
     */
    void batchSave(Collection<T> beans, boolean block) throws Exception;

    /**
     * 阻塞保存数据
     * 
     * @param bean
     * @return
     */
    boolean blockSave(T bean) throws Exception;

    Class<T> getBeanClazz();

    /**
     * 加载所有数据
     * 
     * @return
     * @throws Exception
     */
    List<T> loadAll() throws Exception;

    /**
     * 获取指定主键的数据<br>
     * 若缓存中存在 则取缓存中的数据
     * 
     * @param key
     * @return
     * @throws Exception
     */
    T loadBean(Object key) throws Exception;

    /**
     * 获取指定索引键的数据<br>
     * 
     * @param key
     * @return
     * @throws Exception
     */
    List<T> loadListByIndexId(Object key) throws Exception;

    /**
     * 保存数据
     * 
     * @param bean
     * @param block 是否阻塞保存
     * @return
     */
    boolean save(T bean, boolean block) throws Exception;

}
