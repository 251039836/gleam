package gleam.config.container;

import gleam.config.GameConfig;

/**
 * 远程游戏配置容器<br>
 * 该配置需要http读取远程文件<br>
 * 并且需要周期刷新
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface RemoteConfigContainer<T extends GameConfig> extends ConfigContainer<T> {

    String REMOTE_CONFIG_HOST_KEY = "static.info.host";
    String REMOTE_CONFIG_PORT_KEY = "static.info.port";

    /**
     * 远程配置的路径
     * 
     * @return
     */
    String getRemoteConfigUrl();

}
