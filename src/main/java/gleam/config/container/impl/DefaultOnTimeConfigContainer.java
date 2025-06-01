package gleam.config.container.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import gleam.config.OnTimeConfig;

/**
 * 默认本地带有上架时间的配置容器<br>
 * 配置来源为本地的json文件
 *
 * @param <T>
 * @author hdh
 */
public class DefaultOnTimeConfigContainer<T extends OnTimeConfig> extends DefaultLocalConfigContainer<T> {

    /**
     * 上架服装配置刷新间隔 每十分钟的整数点刷新检查配置
     */
    protected final static long REFRESH_INTERVAL = TimeUnit.MINUTES.toSeconds(10);

    /**
     * 上次刷新时间
     */
    protected AtomicLong lastTime = new AtomicLong();
    /**
     * 是否含有上架时间相关的配置
     */
    protected boolean hadOnTime;
    /**
     * 已上架的配置map
     */
    protected Map<Integer, T> onTimeMap = new HashMap<>();
    /**
     * 未解锁的配置map
     */
    protected Map<Integer, T> lockMap = new HashMap<>();

    public DefaultOnTimeConfigContainer(Class<T> configClazz) {
        super(configClazz);
    }

    /**
     * 检查并刷新上架的服装配置
     */
    protected void checkAndRefresh() {
        if (!hadOnTime && lockMap.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastTimeValue = lastTime.get();
        if (now < lastTimeValue + REFRESH_INTERVAL) {
            long multi1 = now / REFRESH_INTERVAL;
            long multi2 = lastTimeValue / REFRESH_INTERVAL;
            if (multi1 == multi2) {
                // 同1个刷新间隔内 不重复刷新
                return;
            }
        }
        if (!lastTime.compareAndSet(lastTimeValue, now)) {
            return;
        }
        refresh(now);
    }

    @Override
    public Map<Integer, T> getAllConfigs() {
        if (!hadOnTime) {
            return configMap;
        }
        checkAndRefresh();
        return onTimeMap;
    }

    @Override
    public T getConfig(int id) {
        T config = configMap.get(id);
        if (config == null) {
            return null;
        }
        int onTime = config.getUploadTime();
        if (!isValidTime(onTime)) {
            return null;
        }
        return config;
    }

    /**
     * 判断传入时间是否越过当前时间\ 是否是上架时间
     */
    protected boolean isValidTime(int time) {
        long now = System.currentTimeMillis();
        return isValidTime(time, now);
    }

    /**
     * 判断传入时间是否越过当前时间\ 是否是上架时间
     */
    protected boolean isValidTime(int time, long now) {
        if (time == 0) {
            return true;
        }
        if (time < 0) {
            return false;
        }
        return now >= TimeUnit.SECONDS.toMillis(time);
    }

    @Override
    public synchronized void load(String content) throws Exception {
        long now = System.currentTimeMillis();
        lastTime.set(now);
        List<T> tmpList = parseConfigs(content);
        for (T config : tmpList) {
            int uploadTime = config.getUploadTime();
            int id = config.getId();
            configMap.put(id, config);
            boolean unlock = isValidTime(uploadTime, now);
            if (!unlock) {
                // 未解锁
                lockMap.put(id, config);
                hadOnTime = true;
            } else {
                // 已解锁
                lockMap.remove(id);
                onTimeMap.put(id, config);
            }
        }
    }

    protected synchronized void refresh(long now) {
        Map<Integer, T> newMap = null;
        boolean change = false;
        for (Iterator<T> iterator = lockMap.values().iterator(); iterator.hasNext();) {
            T tmpConfig = iterator.next();
            int uploadTime = tmpConfig.getUploadTime();
            if (!isValidTime(uploadTime, now)) {
                continue;
            }
            iterator.remove();
            change = true;
            if (newMap == null) {
                newMap = new HashMap<>(onTimeMap);
            }
            newMap.putIfAbsent(tmpConfig.getId(), tmpConfig);
        }
        if (change) {
            onTimeMap = newMap;
            if (lockMap.isEmpty()) {
                hadOnTime = false;
            }
        }
    }

}
