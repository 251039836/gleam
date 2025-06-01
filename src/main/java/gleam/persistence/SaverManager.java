package gleam.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gleam.core.service.AbstractService;
import gleam.task.TaskManager;

public class SaverManager extends AbstractService implements SaverService {

    private static SaverManager instance = new SaverManager();

    public static SaverManager getInstance() {
        return instance;
    }

    private TaskManager taskManager;

    private final Map<Class<?>, Saver<?>> saverMap = new HashMap<>();

    @Override
    public int getPriority() {
        return PRIORITY_HIGHEST;
    }

    @Override
    protected void onInitialize() throws Exception {
        taskManager = TaskManager.buildSmallInstance("saver");
    }

    @Override
    protected void onStart() {
        for (Entry<Class<?>, Saver<?>> entry : saverMap.entrySet()) {
            try {
                Saver<?> saver = entry.getValue();
                saver.startup();
            } catch (Exception e) {
                logger.error("saver[" + entry.getKey() + "] startup error.", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 该模块需要晚于其他模块销毁
        for (Entry<Class<?>, Saver<?>> entry : saverMap.entrySet()) {
            try {
                Saver<?> saver = entry.getValue();
                saver.shutdown();
            } catch (Exception e) {
                logger.error("saver[" + entry.getKey() + "] shutdown error.", e);
            }
        }
    }

    @Override
    public <T> void asyncSave(Class<T> beanClazz, Collection<T> beans) throws Exception {
        if (beanClazz == null) {
            throw new NullPointerException("beanClazz is null.");
        }
        if (beans == null || beans.isEmpty()) {
            return;
        }
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("asyncSave error.saver is null");
            throw new NullPointerException();
        }
        saver.batchSave(beans, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void asyncSave(T bean) {
        if (bean == null) {
            return;
        }
        Class<T> beanClazz = (Class<T>) bean.getClass();
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("blockSave error.saver is null");
            throw new NullPointerException();
        }
        saver.asyncSave(bean);
    }

    @Override
    public <T> void blockSave(Class<T> beanClazz, Collection<T> beans) throws Exception {
        if (beanClazz == null) {
            throw new NullPointerException("beanClazz is null.");
        }
        if (beans == null || beans.isEmpty()) {
            return;
        }
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("asyncSave error.saver is null");
            throw new NullPointerException();
        }
        saver.batchSave(beans, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void blockSave(T bean) throws Exception {
        if (bean == null) {
            return;
        }
        Class<T> beanClazz = (Class<T>) bean.getClass();
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("blockSave error.saver is null");
            throw new NullPointerException();
        }
        saver.blockSave(bean);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Saver<T>, T> S getSaver(Class<T> clazz) {
        Saver<?> saver = saverMap.get(clazz);
        if (saver == null) {
            return null;
        }
        return (S) saver;
    }

    @Override
    public <T> List<T> loadAll(Class<T> beanClazz) throws Exception {
        if (beanClazz == null) {
            throw new NullPointerException("beanClazz is null.");
        }
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("load bean[{}] error.saver is null.", beanClazz.getSimpleName());
            throw new NullPointerException();
        }
        List<T> beanList = saver.loadAll();
        return beanList;
    }

    @Override
    public <T> T loadBean(Class<T> beanClazz, Object key) throws Exception {
        if (beanClazz == null) {
            throw new NullPointerException("beanClazz is null.");
        }
        if (key == null) {
            throw new NullPointerException("load bean error.key is null.");
        }
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("load bean[{}] error.saver is null.", beanClazz.getSimpleName());
            throw new NullPointerException();
        }
        T bean = saver.loadBean(key);
        return bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void save(T bean, boolean block) throws Exception {
        if (bean == null) {
            return;
        }
        Class<T> beanClazz = (Class<T>) bean.getClass();
        Saver<T> saver = getSaver(beanClazz);
        if (saver == null) {
            logger.error("blockSave error.saver is null");
            throw new NullPointerException();
        }
        saver.save(bean, block);
    }

    public <T> void registerSaver(Class<T> clazz, Saver<T> saver) {
        saverMap.put(clazz, saver);
    }

    public Map<Class<?>, Saver<?>> getSaverMap() {
        return saverMap;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

}
