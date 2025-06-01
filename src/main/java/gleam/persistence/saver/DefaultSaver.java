package gleam.persistence.saver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import gleam.persistence.Changeable;
import gleam.persistence.Dao;
import gleam.persistence.PersistenceData;
import gleam.persistence.define.PersistenceConstant;

public class DefaultSaver<T extends PersistenceData> extends AbstractSaver<T> {

    protected final Dao<T> dao;

    /**
     * 待保存的数据
     */
    protected final ConcurrentMap<Object, DataClip<T>> dataClips = new ConcurrentHashMap<>();

    public DefaultSaver(Dao<T> dao) {
        this(PersistenceConstant.DEFAULT_SAVER_TICK_INTERVAL, dao);
    }

    public DefaultSaver(long tickInterval, Dao<T> dao) {
        super(tickInterval);
        if (dao == null) {
            throw new IllegalArgumentException("dao is null.");
        }
        this.dao = dao;
    }

    @Override
    public void asyncSave(T data) {
        if (data == null) {
            throw new NullPointerException("asyncSave error.data is null.");
        }
        Object key = data.getPrimaryKey();
        if (key == null) {
            throw new NullPointerException("asyncSave error.key is null.");
        }
        if (data instanceof Changeable) {
            Changeable changeable = (Changeable) data;
            changeable.setChange(false);
        }
        dataClips.put(key, new DataClip<>(data));
    }

    @Override
    public void batchSave(Collection<T> datas, boolean block) throws Exception {
        if (datas == null || datas.isEmpty()) {
            return;
        }
        if (block) {
            dao.batchSave(datas);
            for (T data : datas) {
                if (data instanceof Changeable) {
                    Changeable changeable = (Changeable) data;
                    changeable.setChange(false);
                }
            }
        } else {
            for (T data : datas) {
                Object key = data.getPrimaryKey();
                if (key == null) {
                    logger.error("async batch save [{}] fail.key is null.", ReflectionToStringBuilder.toString(data));
                    continue;
                }
                if (data instanceof Changeable) {
                    Changeable changeable = (Changeable) data;
                    changeable.setChange(false);
                }
                dataClips.put(key, new DataClip<>(data));
            }
        }
    }

    @Override
    public boolean blockSave(T data) throws Exception {
        if (data == null) {
            throw new NullPointerException("blockSave error.data is null.");
        }
        Object key = data.getPrimaryKey();
        if (key == null) {
            throw new NullPointerException("blockSave error.key is null.");
        }
        boolean result = dao.save(data);
        if (result && data instanceof Changeable) {
            Changeable changeData = (Changeable) data;
            changeData.setChange(false);
        }
        return result;
    }

    @Override
    protected void doSaveAll(boolean shutdown) {
        if (dataClips.isEmpty()) {
            return;
        }
        Set<Object> waitSaveKeys = dataClips.keySet();
        for (Object key : waitSaveKeys) {
            DataClip<T> clip = dataClips.get(key);
            if (clip == null) {
                continue;
            }
            if (!clip.isTimeToSave()) {
                continue;
            }
            T data = clip.getData();
            boolean success = false;
            try {
                success = dao.save(data);
            } catch (Exception e) {
                logger.error("save data error.key[{}],errorCount[{}]", key, clip.getErrorCount() + 1, e);
                success = false;
            }
            if (success) {
                // 保存成功
                // 若新保存数据 会new另外个clip 不是同1个对象 remove会失败
                dataClips.remove(key, clip);
            } else {
                // 保存失败
                clip.addErrorCount();
            }
        }
        if (shutdown && !dataClips.isEmpty()) {
            String keysStr = StringUtils.join(waitSaveKeys, ',');
            logger.error("shutdown save fail.failList size:{},keys:[{}]", dataClips.size(), keysStr);
            // 生成文件
            for (DataClip<T> clip : dataClips.values()) {
                T data = clip.getData();
                save2File(data);
            }
        }
    }

    @Override
    public Class<T> getBeanClazz() {
        return dao.getDataClazz();
    }

    public Dao<T> getDao() {
        return dao;
    }

    public ConcurrentMap<Object, DataClip<T>> getDataClips() {
        return dataClips;
    }

    public Map<Object, T> getWaitSaveDatas() {
        if (dataClips.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, T> result = new HashMap<>();
        for (Entry<Object, DataClip<T>> entry : dataClips.entrySet()) {
            Object key = entry.getKey();
            T data = entry.getValue().getData();
            result.put(key, data);
        }
        return result;
    }

    @Override
    public List<T> loadAll() throws Exception {
        return dao.selectAll();
    }

    @Override
    public T loadBean(Object key) throws Exception {
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        DataClip<T> clip = dataClips.get(key);
        if (clip != null) {
            return clip.getData();
        }
        T data = dao.select(key);
        return data;
    }

    @Override
    public List<T> loadListByIndexId(Object key) throws Exception {
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        return dao.selectByIndexId(key);
    }

}
