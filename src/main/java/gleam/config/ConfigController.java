package gleam.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.annotation.Param;
import gleam.communication.http.annotation.RequestMapping;
import gleam.communication.http.helper.SimpleHttpResponse;
import gleam.config.container.ConfigContainer;
import gleam.config.container.RemoteConfigContainer;

@RequestMapping("/config")
public class ConfigController {
	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

	@RequestMapping("/refresh")
	public SimpleHttpResponse refresh(@Param("file") String file) {

		Map<Class<? extends GameConfig>, ConfigContainer<?>> containers = ConfigManager.getInstance().getContainers();
		RemoteConfigContainer<?> container = null;
		for (ConfigContainer<?> tmpContainer : containers.values()) {
			if (!(tmpContainer instanceof RemoteConfigContainer)) {
				continue;
			}
			String tmpFileName = tmpContainer.getFileName();
			if (!StringUtils.equals(tmpFileName, file)) {
				continue;
			}
			container = (RemoteConfigContainer<?>) tmpContainer;
		}
		if (container == null) {
			logger.warn("config[{}] container refresh fail.cant find config container.", file);
			return SimpleHttpResponse.failed("remote config[" + file + "] not exists.");
		}
		try {
			container.reloadAll(true);
		} catch (Exception e) {
			logger.error("force refresh remote config[{}] error", file, e);
			return SimpleHttpResponse.failed("remote config[" + file + "] refresh error." + e.getMessage());
		}
		container.afterLoad(false);
		logger.info("config[{}] container refresh success.url:{}", container.getConfigClazz().getName(),
				container.getRemoteConfigUrl());
		return SimpleHttpResponse.success();
	}
}
