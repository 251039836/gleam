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
import gleam.persistence.saver.converter.PersistenceDataConverter;

/**
 * 数据延迟转换保存器<br>
 * 原始数据在实际执行保存操作才转为持久化数据<br>
 * 转换逻辑在尝试写入数据库的线程中执行
 * 
 * @author hdh
 *
 * @param <B>
 * @param <D>
 */
public class DelayConvertSaver<B, D extends PersistenceData> extends AbstractConvertSaver<B, D> {

    /**
     * 待保存的数据
     */
    protected final ConcurrentMap<Object, DataClip<B>> beanClips = new ConcurrentHashMap<>();

    public DelayConvertSaver(Dao<D> dao, PersistenceDataConverter<B, D> converter) {
        super(PersistenceConstant.DEFAULT_SAVER_TICK_INTERVAL, dao, converter);
    }

    public DelayConvertSaver(long tickInterval, Dao<D> dao, PersistenceDataConverter<B, D> converter) {
        super(tickInterval, dao, converter);
    }

    @Override
    public void asyncSave(B bean) {
        if (bean == null) {
            throw new NullPointerException("asyncSave error.bean is null.");
        }
        Object key = converter.getKey(bean);
        if (key == null) {
            throw new NullPointerException("asyncSave error.key is null.");
        }
        if (bean instanceof Changeable) {
            Changeable changeable = (Changeable) bean;
            changeable.setChange(false);
        }
        beanClips.put(key, new DataClip<>(bean));
    }

    @Override
    public void batchSave(Collection<B> beans, boolean block) throws Exception {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        if (block) {
            List<D> dataList = converter.beans2Datas(beans);
            dao.batchSave(dataList);
            for (B bean : beans) {
                if (bean instanceof Changeable) {
                    Changeable changeable = (Changeable) bean;
                    changeable.setChange(false);
                }
            }
        } else {
            for (B bean : beans) {
                Object key = converter.getKey(bean);
                if (key == null) {
                    logger.error("async batch save [{}] fail.key is null.", ReflectionToStringBuilder.toString(bean));
                    continue;
                }
                if (bean instanceof Changeable) {
                    Changeable changeable = (Changeable) bean;
                    changeable.setChange(false);
                }
                beanClips.put(key, new DataClip<>(bean));
            }
        }
    }

    @Override
    protected void doSaveAll(boolean shutdown) {
        if (beanClips.isEmpty()) {
            return;
        }
        Set<Object> waitSaveKeys = beanClips.keySet();
        for (Object key : waitSaveKeys) {
            DataClip<B> clip = beanClips.get(key);
            if (clip == null) {
                continue;
            }
            if (!clip.isTimeToSave()) {
                continue;
            }
            B bean = clip.getData();
            boolean success = false;
            try {
                D data = converter.bean2Data(bean);
                success = dao.save(data);
            } catch (Exception e) {
                logger.error("save data error.key[{}],errorCount[{}]", key, clip.getErrorCount() + 1, e);
                success = false;
            }
            if (success) {
                // 保存成功
                // 若新保存数据 会new另外个clip 不是同1个对象 remove会失败
                beanClips.remove(key, clip);
            } else {
                // 保存失败
                clip.addErrorCount();
            }
        }
        if (shutdown && !beanClips.isEmpty()) {
            String keysStr = StringUtils.join(waitSaveKeys, ',');
            logger.error("shutdown save fail.failList size:{},keys:[{}]", beanClips.size(), keysStr);
            // 生成文件
            for (DataClip<B> clip : beanClips.values()) {
                B bean = clip.getData();
                D data = converter.bean2Data(bean);
                save2File(data);
            }
        }
    }

    public ConcurrentMap<Object, DataClip<B>> getBeanClips() {
        return beanClips;
    }

    public Map<Object, B> getWaitSaveBeans() {
        if (beanClips.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, B> result = new HashMap<>();
        for (Entry<Object, DataClip<B>> entry : beanClips.entrySet()) {
            Object key = entry.getKey();
            B bean = entry.getValue().getData();
            result.put(key, bean);
        }
        return result;
    }

    @Override
    public B loadBean(Object key) throws Exception {
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        DataClip<B> clip = beanClips.get(key);
        if (clip != null) {
            return clip.getData();
        }
        D data = dao.select(key);
        if (data == null) {
            return null;
        }
        B bean = converter.data2Bean(data);
        return bean;
    }
}
