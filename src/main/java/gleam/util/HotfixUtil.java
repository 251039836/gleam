package gleam.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.hotswap.HotswapUtil;
import gleam.config.ConfigManager;
import gleam.config.container.ConfigContainer;
import gleam.util.script.ScriptUtil;

/**
 * 热更工具类
 * 
 * @author hdh
 *
 */
public class HotfixUtil {
	private final static Logger logger = LoggerFactory.getLogger(HotfixUtil.class);

	/**
	 * 需要热更的class文件存放的目录
	 */
	private final static String HOTSWAP_CLASS_DIR_PATH = "hotfix/class";
	/**
	 * 需要执行的脚本文件存放的目录
	 */
	private final static String HOTFIX_SCRIPT_DIR_PATH = "hotfix/script";
	/**
	 * 默认执行的脚本文件名
	 */
	private final static String DEFAULT_SCRIPT_FILE = "script.java";
	/**
	 * 重加载配置列表分隔符
	 */
	private static final String RELOAD_JSON_SPLIT_CHAR = ",";

	/**
	 * 热替换类文件
	 * 
	 * @return
	 */
	public static String hotswapClass() {
		logger.warn("hotswapClass begin");
		try {
			File dirFile = new File(HOTSWAP_CLASS_DIR_PATH);
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				String result = "hotswapClass error,dir not exists.";
				logger.error(result);
				return result;
			}
			File[] childFiles = dirFile.listFiles();
			List<File> clazzFiles = new ArrayList<>();
			for (File childFile : childFiles) {
				if (childFile.isDirectory()) {
					continue;
				}
				String fileName = childFile.getName();
				if (fileName.endsWith(ClazzUtil.CLASS_FILE_SUBFIX)) {
					clazzFiles.add(childFile);
				}
			}
			if (clazzFiles.isEmpty()) {
				String result = "hotswapClass error,clazzFiles is empty.";
				logger.error(result);
				return result;
			}
			boolean success = HotswapUtil.replaceClasses(clazzFiles.toArray(new File[clazzFiles.size()]));
			String result = "hotswapClass end.result=" + (success ? "success" : "fail");
			logger.warn(result);
			return result;
		} catch (Throwable e) {
			logger.error("hotswapClass error.", e);
			return "hotswapClass error." + e.getCause().toString();
		}
	}

	/**
	 * 重加载json配置<br>
	 * 需要重新打包 更新json目录<br>
	 * 执行脚本时 直接尝试重新加载json目录下最新的配置
	 * 
	 * @param cmd 需要重新加载的json文件名列表 以,分割
	 * @return
	 */
	public static String reloadJson(String cmd) {
		logger.warn("reloadJson start.");
		if (StringUtils.isBlank(cmd)) {
			logger.error("reloadJson error.fileNames is null.");
			return "reloadJson error.fileNames is null.";
		}
		try {
			String[] fileNames = cmd.split(RELOAD_JSON_SPLIT_CHAR);
			List<ConfigContainer<?>> containers = new ArrayList<>();
			for (String fileName : fileNames) {
				ConfigContainer<?> container = ConfigManager.getInstance().getContainersByName(fileName);
				containers.add(container);
				if (container == null) {
					logger.error("reload get config container[{}] error.container not exists.", fileName);
					return "reloadJson error.config[" + fileName + "] container not exists.";
				}
			}
			// 热更配置
			for (ConfigContainer<?> container : containers) {
				container.reloadAll(true);
				container.afterLoad(false);
				logger.warn("reload json[{}] success.", container.getFileName());
			}
			String result = "reloadJson success.";
			logger.warn(result);
			return result;
		} catch (Exception e) {
			logger.error("reloadJson error.", e);
			return "reloadJson error." + e.getCause().toString();
		}
	}

	/**
	 * 执行指定脚本
	 * 
	 * @param scriptName
	 * @return
	 */
	public static String runScript(String scriptName) {
		if (StringUtils.isEmpty(scriptName)) {
			scriptName = DEFAULT_SCRIPT_FILE;
			logger.warn("run default script[{}] start.", scriptName);
		} else {
			logger.warn("run script[{}] start.", scriptName);
		}
		File scriptFile = ResourceUtil.getFile(HOTFIX_SCRIPT_DIR_PATH + "/" + scriptName);
		if (!scriptFile.exists() || !scriptFile.isFile()) {
			String result = "runScript[" + scriptName + "] error.script not exists.";
			logger.error(result);
			return result;
		}
		try {
			String scriptCode = FileUtils.readFileToString(scriptFile, StandardCharsets.UTF_8);
			if (scriptCode == null || scriptCode.isEmpty()) {
				String result = "runScript[" + scriptName + "] error.scriptCode is blank.";
				logger.error(result);
				return result;
			}
			Object result = ScriptUtil.executeGroovyScriptClass(scriptCode);
			String print = "runScript[" + scriptName + "] success.result=" + result;
			// 执行结束后手动执行fullgc 避免脚本操作过程加载大量临时数据 内存不及时施放导致机器内存不足
			System.gc();
			logger.warn(print);
			return print;
		} catch (Exception e) {
			logger.error("runScript[{}] error.", scriptName, e);
			return "runScript[" + scriptName + "] error." + e.getCause().toString();
		}
	}

	/**
	 * 获取当前版本号
	 * 
	 * @return
	 */
	public static String version() {
		String releaseVersion = CodeVersionHelper.getReleaseVersion();
		StringBuffer sb = new StringBuffer();
		sb.append("releaseVersion[").append(releaseVersion).append(']');
		if (StringUtils.isBlank(releaseVersion)
				|| StringUtils.equals(releaseVersion, CodeVersionHelper.UNKNOWN_VERSION)) {
			String codeVersion = CodeVersionHelper.getCodeVersion();
			sb.append(" codeVersion[").append(codeVersion).append(']');
		} else {
			String releaseCodeVersion = CodeVersionHelper.getReleaseCodeVersion();
			String releaseJsonVersion = CodeVersionHelper.getReleaseJsonVersion();
			sb.append(" codeVersion[").append(releaseCodeVersion).append(']');
			sb.append(" jsonVersion[").append(releaseJsonVersion).append(']');
		}
		String result = sb.toString();
		logger.warn(result);
		return result;
	}

}
