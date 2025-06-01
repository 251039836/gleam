package gleam.config.container;

import java.util.Map;

import gleam.config.ConfigManager;
import gleam.config.GameConfig;
import gleam.config.backstage.RefreshRemoteConfigMsgHandler;

/**
 * 游戏配置容器<br>
 * 保存一种配置的容器
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface ConfigContainer<T extends GameConfig> {
    /**
     * 加载完成后执行<br>
     * {@link #loadAll()}或{@link #reloadAll(boolean)}发生变化时 执行
     * 
     * @param startup 是否启动时调用
     */
    void afterLoad(boolean startup);

    /**
     * 获取所有配置
     * 
     * @return
     */
    Map<Integer, T> getAllConfigs();

    /**
     * 根据配置id获取配置
     * 
     * @param id
     * @return
     */
    T getConfig(int id);

    /**
     * 对应的协议类
     * 
     * @return
     */
    Class<T> getConfigClazz();

    /**
     * 对应的配置文件名
     * 
     * @return
     */
    String getFileName();

    /**
     * 获取唯一的配置<br>
     * 若该配置类对应的配置只有1条 且id为1<br>
     * 可使用该方式获取<br>
     * 若有多条 返回错误
     * 
     * @return
     */
    T getUnique();

    /**
     * 加载相关的所有配置
     * 
     * @throws Exception
     */
    void loadAll() throws Exception;

    /**
     * 尝试重新加载相关的所有配置<br>
     * 本地配置 执行热更配置命令调用<br>
     * 远程配置定时调用 或后台通知 直接调用<br>
     * 定时任务刷新时{@link ConfigManager#refreshRemoteConfigs()} 返回true时
     * 或后台调用时{@link RefreshRemoteConfigMsgHandler}<br>
     * 
     * @param force 是否强制执行 后台调用/热更配置时为force
     * @return 是否执行了重新加载
     * @throws Exception
     */
    boolean reloadAll(boolean force) throws Exception;

}
