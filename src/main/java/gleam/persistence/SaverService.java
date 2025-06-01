package gleam.persistence;

import java.util.Collection;
import java.util.List;

import gleam.core.service.Service;

public interface SaverService extends Service {

    /**
     * 异步保存数据
     * 
     * @param <T>
     * @param bean
     * @throws Exception
     */
    public <T> void asyncSave(T bean);

    /**
     * 阻塞保存数据
     * 
     * @param <T>
     * @param bean
     * @throws Exception
     */
    public <T> void blockSave(T bean) throws Exception;

    /**
     * 保存数据
     * 
     * @param <T>
     * @param bean
     * @param block 是否阻塞当前线程
     * @throws Exception
     */
    public <T> void save(T bean, boolean block) throws Exception;

    /**
     * 异步批量保存数据
     * 
     * @param <T>
     * @param beanClazz
     * @param beans
     * @throws Exception
     */
    public <T> void asyncSave(Class<T> beanClazz, Collection<T> beans) throws Exception;

    /**
     * 阻塞批量保存数据
     * 
     * @param <T>
     * @param beanClazz
     * @param beans
     * @throws Exception
     */
    public <T> void blockSave(Class<T> beanClazz, Collection<T> beans) throws Exception;

    /**
     * 加载该类型所有数据
     * 
     * @param <T>
     * @param beanClazz
     * @return
     * @throws Exception
     */
    public <T> List<T> loadAll(Class<T> beanClazz) throws Exception;

    /**
     * 加载指定类型的指定数据
     * 
     * @param <T>
     * @param beanClazz
     * @param key
     * @return
     * @throws Exception
     */
    public <T> T loadBean(Class<T> beanClazz, Object key) throws Exception;

    /**
     * 获取该数据类型的保存器
     * 
     * @param <S>
     * @param <T>
     * @param clazz
     * @return
     */
    public <S extends Saver<T>, T> S getSaver(Class<T> clazz);
}
