package gleam.redis.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.redis.RedisCache;
import gleam.redis.RedisRepostory;
import gleam.redis.RedisService;
import gleam.util.ClazzUtil;

/**
 * RedisTemplate 封装对一个map的基本操作, 如果有额外操作, 子类自定义实现
 * 
 * @author Jeremy
 * @param <K> 主键
 * @param <V> 实体
 */
public abstract class AbstractRedisRepostory<K, V> implements RedisRepostory<K, V> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Class<K> keyClazz;

    protected final Class<V> valueClazz;
    /**
     * redis中的表名
     */
    protected final String cacheName;
    /**
     * 因极端情况导致数据未同步到redis服务器, 记录在这里, 当服务器连接成功后, 合并数据同步
     */
    protected final ConcurrentMap<K, V> syncData = new ConcurrentHashMap<>();

    protected RMap<K, V> rmap;

    @SuppressWarnings("unchecked")
    public AbstractRedisRepostory() {
        Class<?>[] parameterizedTypeClazzes = ClazzUtil.getParameterizedTypeClazzes(getClass(), AbstractRedisRepostory.class);
        if (parameterizedTypeClazzes == null || parameterizedTypeClazzes.length < 2) {
            throw new NullPointerException();
        }
        keyClazz = (Class<K>) parameterizedTypeClazzes[0];
        valueClazz = (Class<V>) parameterizedTypeClazzes[1];
        cacheName = initCacheName();
    }

    public AbstractRedisRepostory(Class<K> keyClazz, Class<V> valueClazz) {
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        cacheName = initCacheName();
    }

    /**
     * 创建redission的map实现类
     * 
     * @param cacheName
     * @return
     */
    protected RMap<K, V> buildRmap(String cacheName) {
        RedissonClient redissonClient = RedisService.getInstance().getRedissonClient();
        Codec codec = getCodec();
        return redissonClient.getMap(cacheName, codec);
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    protected Codec getCodec() {
        CommonRedisMapCodec<K, V> codec = new CommonRedisMapCodec<>(keyClazz, valueClazz);
        return codec;
    }

    @Override
    public Class<K> getKeyClazz() {
        return keyClazz;
    }

    @Override
    public Map<K, V> getMap() {
        return rmap;
    }

    /**
     * 获取数据, 如果redis server断开了连接, 则返回null
     */
    @Override
    public V getObject(K k) {
        try {
            return rmap.get(k);
        } catch (Exception e) {
            logger.error("获取数据源出错, 检查连接是否中断,", e);
            return syncData.get(k);
        }
    }

    @Override
    public Class<V> getValueClazz() {
        return valueClazz;
    }

    @Override
    public void init() {
        rmap = buildRmap(cacheName);
    }

    /**
     * 初始化表名
     * 
     * @return
     */
    protected String initCacheName() {
        RedisCache redisCache = valueClazz.getAnnotation(RedisCache.class);
        String cacheName = valueClazz.getSimpleName();
        if (redisCache != null && !StringUtils.isBlank(redisCache.value())) {
            cacheName = redisCache.value();
        }
        return cacheName;
    }

    protected abstract boolean merge(V val);

    /**
     * syncData只有在断线异常情况下才会出现修改情况
     */
    @Override
    public void mergeAll() {
        if (syncData.isEmpty())
            return;

        syncData.forEach((key, val) -> {
            merge(val);
        });
        // logger.info("mergeAll.syncData:{}", syncData);
        syncData.clear();
    }

    /**
     * 更新数据, 如果redis server断开了连接, 则缓存起来, 等到redis server启动时,重新更新到redis server
     */
    @Override
    public void putObject(K k, V t) {
        try {
            rmap.put(k, t);
        } catch (Exception e) {
            logger.error("获取数据源出错, 检查连接是否中断,", e);
            syncData.put(k, t);
        }
    }

}
