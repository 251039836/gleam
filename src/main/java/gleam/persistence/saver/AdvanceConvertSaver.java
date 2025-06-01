package gleam.persistence.saver;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
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
 * 数据提前转换保存器<br>
 * 尝试保存数据时 就直接把原始数据转为持久化数据<br>
 * 转换逻辑在调用线程中执行<br>
 * 
 * @author hdh
 *
 * @param <B>
 * @param <D>
 */
public class AdvanceConvertSaver<B, D extends PersistenceData> extends AbstractConvertSaver<B, D> {

    /**
     * 待保存的数据
     */
    protected final ConcurrentMap<Object, DataClip<D>> dataClips = new ConcurrentHashMap<>();

    public AdvanceConvertSaver(Dao<D> dao, PersistenceDataConverter<B, D> converter) {
        super(PersistenceConstant.DEFAULT_SAVER_TICK_INTERVAL, dao, converter);
    }

    public AdvanceConvertSaver(long tickInterval, Dao<D> dao, PersistenceDataConverter<B, D> converter) {
        super(tickInterval, dao, converter);
    }

    @Override
    public void asyncSave(B bean) {
        if (bean == null) {
            throw new NullPointerException("asyncSave error.bean is null.");
        }
        D data = null;
        try {
            data = converter.bean2Data(bean);
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException e) {
            // 转换时 可能因为并发导致转换失败
            // 再尝试次 再失败时再抛出错误
            data = converter.bean2Data(bean);
        }
        if (data == null) {
            throw new NullPointerException("asyncSave error.bean2Data fail.");
        }
        Object key = data.getPrimaryKey();
        if (key == null) {
            throw new NullPointerException("asyncSave error.key is null.");
        }
        if (bean instanceof Changeable) {
            Changeable changeData = (Changeable) bean;
            changeData.setChange(false);
        }
        dataClips.put(key, new DataClip<>(data));
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
                    Changeable changeData = (Changeable) bean;
                    changeData.setChange(false);
                }
            }
        } else {
            for (B bean : beans) {
                D data = converter.bean2Data(bean);
                Object key = data.getPrimaryKey();
                if (key == null) {
                    logger.error("async batch save [{}] fail.key is null.", ReflectionToStringBuilder.toString(data));
                    continue;
                }
                if (bean instanceof Changeable) {
                    Changeable changeData = (Changeable) bean;
                    changeData.setChange(false);
                }
                dataClips.put(key, new DataClip<>(data));
            }
        }
    }

    @Override
    protected void doSaveAll(boolean shutdown) {
        if (dataClips.isEmpty()) {
            return;
        }
        Set<Object> waitSaveKeys = dataClips.keySet();
        for (Object key : waitSaveKeys) {
            DataClip<D> clip = dataClips.get(key);
            if (clip == null) {
                continue;
            }
            if (!clip.isTimeToSave()) {
                continue;
            }
            D data = clip.getData();
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
            for (DataClip<D> clip : dataClips.values()) {
                D data = clip.getData();
                save2File(data);
            }
        }
    }

    public ConcurrentMap<Object, DataClip<D>> getDataClips() {
        return dataClips;
    }

    public Map<Object, D> getWaitSaveDatas() {
        if (dataClips.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, D> result = new HashMap<>();
        for (Entry<Object, DataClip<D>> entry : dataClips.entrySet()) {
            Object key = entry.getKey();
            D data = entry.getValue().getData();
            result.put(key, data);
        }
        return result;
    }

    @Override
    public B loadBean(Object key) throws Exception {
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        D data = null;
        DataClip<D> clip = dataClips.get(key);
        if (clip != null) {
            data = clip.getData();
        } else {
            data = dao.select(key);
        }
        if (data == null) {
            return null;
        }
        B bean = converter.data2Bean(data);
        return bean;
    }
}
