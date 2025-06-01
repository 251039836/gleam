package gleam.config.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.config.GameConfig;
import gleam.exception.NotUniqueConfigException;
import gleam.exception.RepeatConfigException;
import gleam.util.json.JsonUtil;

public abstract class AbstractConfigContainer<T extends GameConfig> implements ConfigContainer<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 配置类
     */
    protected final Class<T> configClazz;
    /**
     * id,配置
     */
    protected Map<Integer, T> configMap = new HashMap<>();

    public AbstractConfigContainer(Class<T> configClazz) {
        this.configClazz = configClazz;
    }

    @Override
    public void afterLoad(boolean startup) {
    }

    @Override
    public Map<Integer, T> getAllConfigs() {
        return configMap;
    }

    @Override
    public T getConfig(int id) {
        return configMap.get(id);
    }

    @Override
    public Class<T> getConfigClazz() {
        return configClazz;
    }

    @Override
    public T getUnique() {
        if (configMap.isEmpty()) {
            return null;
        }
        if (configMap.size() > 1) {
            logger.error("config[{}] size[{}] not unique.", configClazz.getName(), configMap.size());
            throw new NotUniqueConfigException("config[" + configClazz.getName() + "] not unique.");
        }
        return configMap.get(1);
    }

    protected void load(String content) throws Exception {
        List<T> allConfigs = parseConfigs(content);
        putAllConfigs(allConfigs);
        logger.debug("config[{}] load success.size:{}", configClazz.getName(), configMap.size());
    }

    @Override
    public void loadAll() throws Exception {
        String content = readContent();
        load(content);
    }

    /**
     * 解析配置文件内容 转为配置类
     * 
     * @param content 配置文件内容
     * @return
     * @throws IOException
     */
    protected List<T> parseConfigs(String content) throws IOException {
        return JsonUtil.toList(content, configClazz);
    }

    protected synchronized void putAllConfigs(List<T> allConfigs) {
        Map<Integer, T> newConfigMap = new HashMap<>();
        for (T config : allConfigs) {
            int id = config.getId();
            if (newConfigMap.containsKey(id)) {
                throw new RepeatConfigException("config[" + configClazz.getName() + "] has repeat id[" + id + "]");
            }
            newConfigMap.put(id, config);
        }
        configMap = newConfigMap;
    }

    /**
     * 读取配置文本<br>
     * 暂不支持同时读取多个文件/地址的配置
     * 
     * @return
     * @throws Exception
     */
    protected abstract String readContent() throws Exception;

    @Override
    public boolean reloadAll(boolean force) throws Exception {
        String content = readContent();
        load(content);
        return true;
    }

}
