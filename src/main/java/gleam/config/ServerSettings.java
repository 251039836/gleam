package gleam.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import gleam.util.ResourceUtil;
import gleam.util.SafeProperties;

/**
 * 服务器配置<br>
 * 优先读取环境变量{@link System#getenv(String)}<br>
 * 若环境变量中不存在该参数 则读取{@link ServerSettings#SERVER_SETTING_FILE}中的参数
 * 
 * 
 * @author hdh
 *
 */
public class ServerSettings {
	/**
	 * 服务器配置路径
	 */
	public static final String SERVER_SETTING_FILE = "server.conf";
	/**
	 * 日志配置路径
	 */
	private static final String LOG_CONFIG_FILE = "log4j2.xml";

	/**
	 * 地区
	 */
	private final static String REGION_VERSION = "region.version";

	/**
	 * 语言
	 */
	private final static String LANGUAGE_VERSION = "language.version";

	private static final Properties properties = new SafeProperties();

	public static void init() throws IOException, URISyntaxException {
		initSettings();
		initLogger();
	}

	private static void initLogger() throws URISyntaxException {
		URL url = ResourceUtil.getSettingsFileUrl(LOG_CONFIG_FILE);
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.setConfigLocation(url.toURI());
	}

	private static void initSettings() throws IOException {
		URL url = ResourceUtil.getConfFileUrl(SERVER_SETTING_FILE);
		properties.load(url.openStream());
	}

	public static Properties getProperties() {
		return properties;
	}

	public static boolean getBooleanProperty(String key) {
		String value = getPropertyString(key);
		boolean result = Boolean.parseBoolean(value);
		return result;
	}

	/**
	 * 获取boolean值配置, 无则返回给定值
	 * 
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static boolean getBooleanProperty(String key, boolean defaultVal) {
		String value = getProperty(key);
		boolean result = value != null ? Boolean.parseBoolean(value) : defaultVal;
		return result;
	}

	/**
	 * 获取环境变量<br>
	 * 
	 * @param key
	 * @return
	 */
	public static String getEnv(String key) {
		String envKey = key2EnvKey(key);
		return System.getenv(envKey);
	}

	public static int getIntProperty(String key) {
		String value = getPropertyString(key);
		int result = Integer.parseInt(value);
		return result;
	}

	/**
	 * 获取int值配置, 无则返回给定值
	 * 
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static int getIntProperty(String key, int defaultVal) {
		String value = getProperty(key);
		int result = value != null ? Integer.parseInt(value) : defaultVal;
		return result;
	}

	public static long getLongProperty(String key) {
		String value = getPropertyString(key);
		long result = Long.parseLong(value);
		return result;
	}

	/**
	 * 获取long值配置, 无则返回给定值
	 * 
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static long getLongProperty(String key, long defaultVal) {
		String value = getProperty(key);
		long result = value != null ? Long.parseLong(value) : defaultVal;
		return result;
	}

	public static String getProperty(String key) {
		String value = getEnv(key);
		if (value != null) {
			return value;
		}
		return properties.getProperty(key);
	}

	private static String getPropertyString(String key) {
		String value = getEnv(key);
		if (value != null) {
			return value;
		}
		value = properties.getProperty(key);
		if (value == null) {
			throw new NullPointerException(SERVER_SETTING_FILE + " key:" + key + " value is null.");
		}
		return value;
	}

	/**
	 * 获取地区
	 * 
	 * @return
	 */
	public static int getRegion() {
		return ServerSettings.getIntProperty(REGION_VERSION);
	}

	/**
	 * 获取语言
	 * 
	 * @return
	 */
	public static int getLanguage() {
		return getIntProperty(LANGUAGE_VERSION);
	}

	/**
	 * 服务器配置key转为环境变量key<br>
	 * 服务器配置 game.db.host<br>
	 * 缓存变量 GAME_DB_HOST<br>
	 * 
	 * @param key
	 * @return
	 */
	private static String key2EnvKey(String key) {
		String envKey = key.toUpperCase();
		envKey = envKey.replaceAll("\\.", "_");
		return envKey;
	}

	/**
	 * 设置参数并保存
	 * 
	 * @param key
	 * @param value
	 * @param comments
	 * @throws IOException
	 */
	public static void setPropertyAndStore(String key, String value, String comments) throws IOException {
		Properties properties = ServerSettings.getProperties();
		properties.setProperty(key, value);
		// 修改本地配置文件
		URL url = ResourceUtil.getConfFileUrl(SERVER_SETTING_FILE);
		File file = new File(url.getFile());
		try (OutputStream fos = new FileOutputStream(file)) {
			properties.store(fos, comments);
		} finally {
		}
	}
}