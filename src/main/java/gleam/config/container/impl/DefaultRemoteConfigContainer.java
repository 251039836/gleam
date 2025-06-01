package gleam.config.container.impl;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import gleam.config.GameConfig;
import gleam.config.ServerSettings;
import gleam.config.annotation.ConfigUrl;
import gleam.config.container.AbstractConfigContainer;
import gleam.config.container.RemoteConfigContainer;
import gleam.util.MD5Util;
import gleam.util.http.HttpClientUtil;

/**
 * 默认本地配置容器<br>
 * 配置来源为本地的json文件
 * 
 * @author hdh
 *
 * @param <T>
 */
public class DefaultRemoteConfigContainer<T extends GameConfig> extends AbstractConfigContainer<T>
		implements RemoteConfigContainer<T> {

	/**
	 * 本次加载成功时 配置所使用的格式
	 */
	protected String md5;

	public DefaultRemoteConfigContainer(Class<T> configClazz) {
		super(configClazz);
		if (!configClazz.isAnnotationPresent(ConfigUrl.class)) {
			throw new IllegalArgumentException("config[" + configClazz.getName() + "] error.configUrl is null.");
		}
	}

	/**
	 * 生成远程配置的路径
	 * 
	 * @param configName
	 * @return
	 */
	protected String buildRemoteConfigUrl(String configName) {
		Properties properties = ServerSettings.getProperties();
		String host = properties.getProperty(REMOTE_CONFIG_HOST_KEY);
		String port = properties.getProperty(REMOTE_CONFIG_PORT_KEY);
		StringBuffer sb = new StringBuffer();
		sb.append("http://").append(host).append(":").append(port);
		sb.append("/server/").append(configName);
		return sb.toString();
	}

	@Override
	public String getFileName() {
		ConfigUrl configUrl = configClazz.getAnnotation(ConfigUrl.class);
		if (configUrl == null) {
			logger.error("config[{}] url is null.", configClazz.getName());
			throw new NullPointerException("config[" + configClazz.getName() + "] url is null.");
		}
		String fileName = configUrl.value();
		return fileName;
	}

	public String getMd5() {
		return md5;
	}

	@Override
	public String getRemoteConfigUrl() {
		String fileName = getFileName();
		if (StringUtils.isEmpty(fileName)) {
			logger.error("config[{}] fileName is empty.", configClazz.getName());
			throw new NullPointerException("config[" + configClazz.getName() + "] fileName is empty.");
		}
		String url = buildRemoteConfigUrl(fileName);
		return url;
	}

	@Override
	public void load(String content) throws Exception {
		super.load(content);
		// 加载成功才刷新md5
		md5 = MD5Util.toMD5String(content);
	}

	@Override
	public void loadAll() throws Exception {
		// 远程配置加载 给多次机会
		// 极少数情况下 远程配置所在的服务器的带宽被用完 即使有正确的配置也读取不到
		int maxTryCount = 2;
		for (int tryTimes = 1; tryTimes <= maxTryCount; tryTimes++) {
			try {
				super.loadAll();
				break;// 成功了要break
			} catch (Exception e) {
				logger.error("load config[{}] error.times={}", configClazz.getName(), tryTimes, e);
				if (tryTimes >= maxTryCount) {
					ConfigUrl configUrl = configClazz.getAnnotation(ConfigUrl.class);
					if (configUrl == null || configUrl.blockStartup()) {
						throw e;
					}
				}
			}
		}
	}

	@Override
	protected String readContent() throws Exception {
		String url = getRemoteConfigUrl();
		if (StringUtils.isEmpty(url)) {
			logger.error("config[{}] read url[{}] error.url is empty.", configClazz.getName(), url);
			throw new NullPointerException("config[" + configClazz.getName() + "] url is empty.");
		}
		String content = HttpClientUtil.get(url);
		if (StringUtils.isEmpty(content)) {
			// 后台读到的配置为空字符串
			// 视为后台正在清空文件开始写入
			// 直接视为错误
			// 正确的空配置 至少应有[] 或{}
			logger.error("config[{}] read url[{}] fail.content is empty.", configClazz.getName(), url);
			throw new NullPointerException("config[" + configClazz.getName() + "] url[" + url + "] content is empty.");
		}
		return content;
	}

	@Override
	public boolean reloadAll(boolean force) throws Exception {
		try {
			String content = readContent();
			String newMd5 = MD5Util.toMD5String(content);
			if (!force && StringUtils.equals(md5, newMd5)) {
				// 非强制刷新 且2次配置内容一致时<br>
				// 不再刷新
				return false;
			}
			load(content);
			logger.info("config[{}] refresh.md5[{}]", configClazz.getName(), newMd5);
			return true;
		} catch (Exception e) {
			logger.error("config[{}] refresh fail.", configClazz.getName(), e);
			throw e;
		}
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
