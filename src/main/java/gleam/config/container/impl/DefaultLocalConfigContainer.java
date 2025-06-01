package gleam.config.container.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import gleam.config.GameConfig;
import gleam.config.ServerSettings;
import gleam.config.annotation.ConfigPath;
import gleam.config.container.AbstractConfigContainer;
import gleam.util.ResourceUtil;

/**
 * 默认本地配置容器<br>
 * 配置来源为本地的json文件
 * 
 * @author hdh
 *
 * @param <T>
 */
public class DefaultLocalConfigContainer<T extends GameConfig> extends AbstractConfigContainer<T> {

    public final static String LOCAL_CONFIG_DIR = getLocalConfigDir();

    private static String getLocalConfigDir() {
        String configPath = "json";
        int region = ServerSettings.getRegion();
        configPath = configPath + "/" + region;
        int language = ServerSettings.getLanguage();
        configPath = configPath + "/" + language;
        return configPath;
    }

    public DefaultLocalConfigContainer(Class<T> configClazz) {
        super(configClazz);
        if (!configClazz.isAnnotationPresent(ConfigPath.class)) {
            throw new IllegalArgumentException("config[" + configClazz.getName() + "] error.configPath is null.");
        }
    }

    @Override
    public String getFileName() {
        ConfigPath configPath = configClazz.getAnnotation(ConfigPath.class);
        if (configPath == null) {
            logger.error("config[{}] file path is null.", configClazz.getName());
            throw new NullPointerException("config[" + configClazz.getName() + "] file path is null.");
        }
        String localPath = configPath.value();
        return localPath;
    }

    private File getJsonFile() {
        String fileName = getFileName();
        String resource = LOCAL_CONFIG_DIR + "/" + fileName;
        URL url = ResourceUtil.getURL(resource);
        String filePath = url.getFile();
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            logger.error("config[{}] path[{}] file not exists.", configClazz.getName(), fileName);
            throw new NullPointerException("config[" + configClazz.getName() + "] path[" + fileName + "] file not exists.");
        }
        return file;
    }

    @Override
    protected String readContent() throws IOException {
        File file = getJsonFile();
        String configsJson = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return configsJson;
    }

    @Override
    public boolean reloadAll(boolean force) throws Exception {
        if (!force) {
            // 本地配置 不会定时重新加载
            return false;
        }
        return super.reloadAll(force);
    }

}
