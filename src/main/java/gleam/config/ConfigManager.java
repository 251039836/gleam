package gleam.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import gleam.config.annotation.ConfigPath;
import gleam.config.annotation.ConfigUrl;
import gleam.config.container.ConfigContainer;
import gleam.config.container.impl.DefaultLocalConfigContainer;
import gleam.config.container.impl.DefaultOnTimeConfigContainer;
import gleam.config.container.impl.DefaultRemoteConfigContainer;
import gleam.config.register.ConfigRegister;
import gleam.core.service.AbstractService;
import gleam.task.TaskHandle;
import gleam.task.TaskManager;

public class ConfigManager extends AbstractService {

    private static ConfigManager instance = new ConfigManager();

    /**
     * 远程配置刷新间隔
     */
    private final static long REMOTE_CONFIG_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(60);

    public static ConfigManager getInstance() {
        return instance;
    }

    /**
     * 所有配置容器类集合
     */
    private final Map<Class<? extends GameConfig>, ConfigContainer<?>> containers = new HashMap<>();
    /**
     * 刷新远程配置事件
     */
    private TaskHandle refreshRemoteTask;

    private void cancelRefreshRemoteTask() {
        if (refreshRemoteTask != null) {
            refreshRemoteTask.cancel();
        }
        refreshRemoteTask = null;
    }

    /**
     * 获取指定id的配置
     * 
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    public <T extends GameConfig> T get(Class<T> clazz, int id) {
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        return container.getConfig(id);
    }

    public <T extends GameConfig> Map<Integer, T> getAllConfigs(Class<T> clazz) {
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        return container.getAllConfigs();
    }

    /**
     * 获取根据id排序后的该类型所有配置
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends GameConfig> List<T> getAllSortConfigs(Class<T> clazz) {
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        List<T> result = new ArrayList<>(allConfigs.values());
        Collections.sort(result, Comparator.comparingInt(T::getId));
        return result;
    }

    /**
     * 获取排序后的该类型所有配置
     * 
     * @param <T>
     * @param clazz
     * @param comparator
     * @return
     */
    public <T extends GameConfig> List<T> getAllSortConfigs(Class<T> clazz, Comparator<? super T> comparator) {
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        List<T> result = new ArrayList<>(allConfigs.values());
        Collections.sort(result, comparator);
        return result;
    }

    /**
     * 获取满足条件的任意1个配置<br>
     * 若有多个配置满足条件 无法确定会返回哪个配置
     * 
     * @param <T>
     * @param clazz
     * @param predicate 返回true时返回结果
     * @return
     */
    public <T extends GameConfig> T getAnyConfig(Class<T> clazz, Predicate<T> predicate) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (predicate == null) {
            throw new NullPointerException("getAnyConfig error.predicate is null");
        }
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        for (T config : allConfigs.values()) {
            if (predicate.test(config)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 获取指定id的配置
     * 
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    public <T extends GameConfig> T getConfig(Class<T> clazz, int id) {
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return null;
        }
        return container.getConfig(id);
    }

    /**
     * 获取满足条件的配置
     * 
     * @param <T>
     * @param clazz
     * @param predicate 返回true时塞到结果列表中
     * @return
     */
    public <T extends GameConfig> Map<Integer, T> getConfigs(Class<T> clazz, Predicate<T> predicate) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (predicate == null) {
            return getAllConfigs(clazz);
        }
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return Collections.emptyMap();
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        Map<Integer, T> result = new HashMap<>();
        for (T config : allConfigs.values()) {
            if (predicate.test(config)) {
                result.put(config.getId(), config);
            }
        }
        return result;
    }

    public <T extends GameConfig> Map<Integer, T> getConfigsByIds(Class<T> clazz, Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return Collections.emptyMap();
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        Map<Integer, T> result = new HashMap<>();
        for (int id : ids) {
            T config = allConfigs.get(id);
            if (config == null) {
                continue;
            }
            result.put(id, config);
        }
        return result;
    }

    public <T extends GameConfig> Map<Integer, T> getConfigsByIds(Class<T> clazz, int[] ids) {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyMap();
        }
        ConfigContainer<T> container = getContainer(clazz);
        if (container == null) {
            return Collections.emptyMap();
        }
        Map<Integer, T> allConfigs = container.getAllConfigs();
        Map<Integer, T> result = new HashMap<>();
        for (int id : ids) {
            T config = allConfigs.get(id);
            if (config == null) {
                continue;
            }
            result.put(id, config);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends GameConfig> ConfigContainer<T> getContainer(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("getContainer error.clazz is null.");
        }
        ConfigContainer<?> container = containers.get(clazz);
        if (container == null) {
            logger.warn("getContainer[{}] fail.container not exists.", clazz.getName());
            return null;
        }
        return (ConfigContainer<T>) container;
    }

    public Map<Class<? extends GameConfig>, ConfigContainer<?>> getContainers() {
        return containers;
    }

    /**
     * 根据json名称获取对应 container
     * 
     * @param jsonName json名称
     * @return 可返回null
     */
    public ConfigContainer<?> getContainersByName(String jsonName) {
        Map<Class<? extends GameConfig>, ConfigContainer<?>> containers = getContainers();
        for (ConfigContainer<?> container : containers.values()) {
            if (container.getFileName().equals(jsonName)) {
                return container;
            }
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_HIGHEST;
    }

    @Override
    public void onInitialize() throws Exception {
        // loadAll
        for (ConfigContainer<?> container : containers.values()) {
            try {
                container.loadAll();
            } catch (Exception e) {
                logger.error("config[" + container.getConfigClazz().getName() + "] loadAll error.", e);
                throw e;
            }
        }
        // afterLoad
        for (ConfigContainer<?> container : containers.values()) {
            try {
                container.afterLoad(true);
            } catch (Exception e) {
                logger.error("config[" + container.getConfigClazz().getName() + "] afterLoad error.", e);
                throw e;
            }
        }
    }

    @Override
    protected void onStart() {
        // 定时刷新远程配置
        startRefreshRemoteTask();
    }

    @Override
    protected void onStop() {
        cancelRefreshRemoteTask();
    }

    /**
     * 刷新远程配置
     */
    private void refreshRemoteConfigs() {
        for (ConfigContainer<?> container : containers.values()) {
            try {
                boolean refresh = container.reloadAll(false);
                if (refresh) {
                    container.afterLoad(false);
                }
            } catch (Exception e) {
                logger.error("refresh config[{}] error.", container.getFileName(), e);
            }
        }
    }

    /**
     * 注册相关配置
     * 
     * @param register
     */
    public void registerAll(ConfigRegister register) {
        register.registerAll(this);
    }

    @SuppressWarnings("rawtypes")
    public void registerContainer(ConfigContainer<? extends GameConfig> container) {
        Class<? extends GameConfig> configClazz = container.getConfigClazz();
        ConfigContainer<?> oldContainer = containers.put(configClazz, container);
        if (oldContainer == null) {
            return;
        }
        Class<? extends ConfigContainer> curContainer = container.getClass();
        Class<? extends ConfigContainer> otherContainer = oldContainer.getClass();
        if (otherContainer.equals(curContainer)) {
            logger.warn("repeat register config[{}].", configClazz.getName());
        } else {
            logger.error("repeat register config[{}].container1[{}],container2[{}]", configClazz.getName(), otherContainer.getName(), curContainer.getName());
        }
    }

    public void registerContainerIfAbsent(ConfigContainer<? extends GameConfig> container) {
        Class<? extends GameConfig> configClazz = container.getConfigClazz();
        containers.putIfAbsent(configClazz, container);
    }

    /**
     * 注册默认的本地配置<br>
     * 使用默认本地配置容器<br>
     * 若该配置类已注册 则跳过
     * 
     * @param <T>
     * @param configClazz
     */
    public <T extends GameConfig> void registerLocalConfig(Class<T> configClazz) {
        if (!configClazz.isAnnotationPresent(ConfigPath.class)) {
            logger.error("register local config error.configPath is null.clazz[{}]", configClazz);
            throw new NullPointerException("register local config error.configPath is null");
        }
        if (containers.containsKey(configClazz)) {
            return;
        }
        ConfigContainer<T> container = new DefaultLocalConfigContainer<>(configClazz);
        containers.putIfAbsent(configClazz, container);
    }

    public <T extends OnTimeConfig> void registerOnTimeConfig(Class<T> configClazz) {
        if (!configClazz.isAnnotationPresent(ConfigPath.class)) {
            logger.error("register local config error.configPath is null.clazz[{}]", configClazz);
            throw new NullPointerException("register local config error.configPath is null");
        }
        if (containers.containsKey(configClazz)) {
            return;
        }
        ConfigContainer<T> container = new DefaultOnTimeConfigContainer<>(configClazz);
        ConfigContainer<?> otherContainer = containers.putIfAbsent(configClazz, container);
        if (otherContainer != null) {
            logger.warn("repeat register config[{}].container1[{}],container2[{}]", configClazz, otherContainer.getClass(), container.getClass());
        }
    }

    /**
     * 注册默认的远程配置<br>
     * 使用默认远程配置容器
     * 
     * @param <T>
     * @param configClazz
     */
    public <T extends GameConfig> void registerRemoteConfig(Class<T> configClazz) {
        if (!configClazz.isAnnotationPresent(ConfigUrl.class)) {
            logger.error("register remote config error.ConfigUrl is null.clazz[{}]", configClazz);
            throw new NullPointerException("register remote config error.ConfigUrl is null");
        }
        ConfigContainer<T> container = new DefaultRemoteConfigContainer<>(configClazz);
        ConfigContainer<?> otherContainer = containers.putIfAbsent(configClazz, container);
        if (otherContainer != null) {
            logger.warn("repeat register config[{}].container1[{}],container2[{}]", configClazz, otherContainer.getClass(), container.getClass());
        }
    }

    private void startRefreshRemoteTask() {
        cancelRefreshRemoteTask();
        refreshRemoteTask = TaskManager.getInstance().scheduleTask(() -> {
            refreshRemoteConfigs();
        }, REMOTE_CONFIG_REFRESH_INTERVAL, REMOTE_CONFIG_REFRESH_INTERVAL);

    }
}
