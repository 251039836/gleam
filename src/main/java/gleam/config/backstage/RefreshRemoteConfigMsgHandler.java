package gleam.config.backstage;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.echo.BackstageMsgHandler;
import gleam.communication.echo.protocol.BackstageHandlerResp;
import gleam.config.ConfigManager;
import gleam.config.GameConfig;
import gleam.config.container.ConfigContainer;
import gleam.config.container.RemoteConfigContainer;

/**
 * 
 * {‘protocol’:1001, ‘content’:{‘file’: ‘url.json’}}
 * 
 * @author hdh
 *
 */
public class RefreshRemoteConfigMsgHandler implements BackstageMsgHandler<RefreshRemoteConfigMsgHandler.RefreshConfigMsg> {
    public static class RefreshConfigMsg {
        private String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

    }

    private static RefreshRemoteConfigMsgHandler instance = new RefreshRemoteConfigMsgHandler();

    public final static int ID = 1001;
    private final static Logger logger = LoggerFactory.getLogger(RefreshRemoteConfigMsgHandler.class);

    public static RefreshRemoteConfigMsgHandler getInstance() {
        return instance;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public BackstageHandlerResp handler(RefreshConfigMsg msg) {
        String fileName = msg.getFile();
        Map<Class<? extends GameConfig>, ConfigContainer<?>> containers = ConfigManager.getInstance().getContainers();
        RemoteConfigContainer<?> container = null;
        for (ConfigContainer<?> tmpContainer : containers.values()) {
            if (!(tmpContainer instanceof RemoteConfigContainer)) {
                continue;
            }
            String tmpFileName = tmpContainer.getFileName();
            if (!StringUtils.equals(tmpFileName, fileName)) {
                continue;
            }
            container = (RemoteConfigContainer<?>) tmpContainer;
        }
        if (container == null) {
            logger.warn("config[{}] container refresh fail.cant find config container.", fileName);
            return BackstageHandlerResp.failed("remote config[" + fileName + "] not exists.");
        }
        try {
            container.reloadAll(true);
        } catch (Exception e) {
            logger.error("force refresh remote config[{}] error", fileName, e);
            return BackstageHandlerResp.failed("remote config[" + fileName + "] refresh error." + e.getMessage());
        }
        container.afterLoad(false);
        logger.info("config[{}] container refresh success.url:{}", container.getConfigClazz().getName(), container.getRemoteConfigUrl());
        return BackstageHandlerResp.success();
    }

}
