package gleam.redis;

import java.util.Map;

/**
 * redis仓库<br>
 * 对应redis中的hash<br>
 * java中的map
 * 
 * 
 * @author Jeremy
 *
 * @param <K>
 * @param <V>
 */
public interface RedisRepostory<K, V> {

    String getCacheName();

    Class<K> getKeyClazz();

    Map<K, V> getMap();

    /**
     * 通过key, 获取一个对象
     * 
     * @param key
     * @return
     */
    V getObject(K key);

    Class<V> getValueClazz();

    /**
     * 初始化redis仓库
     */
    void init();

    /**
     * 合并所有
     */
    void mergeAll();

    /**
     * 通过key, 保存一个对象
     * 
     * @param key
     * @param value
     * @return
     */
    void putObject(K key, V value);

}
