package gleam.persistence;

import java.util.Collection;
import java.util.List;

/**
 * 数据访问对象<br>
 * 对应单个数据表的操作接口
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface Dao<T extends PersistenceData> {

    Class<T> getDataClazz();

    String getTableName();

    /**
     * 根据主键查找数据
     * 
     * @param key
     * @return
     */
    T select(Object key) throws Exception;

    /**
     * 查找所有数据
     * 
     * @return
     */
    List<T> selectAll() throws Exception;

    /**
     * 获取指定索引键的数据<br>
     * 
     * @param key
     * @return
     * @throws Exception
     */
    List<T> selectByIndexId(Object key) throws Exception;

    /**
     * 插入/更新数据<br>
     * key需要自己生成<br>
     * replace/on duplicate key update操作
     * 
     * @param data
     * @return
     */
    boolean save(T data) throws Exception;

    /**
     * 批量插入/更新数据<br>
     * key需要自己生成<br>
     * replace/on duplicate key update操作
     * 
     * @param dataList
     */
    void batchSave(Collection<T> dataList) throws Exception;

    /**
     * 删除数据
     * 
     * @param key
     * @return
     */
    boolean delete(Object key) throws Exception;
}
