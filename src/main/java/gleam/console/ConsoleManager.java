package gleam.console;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import gleam.core.service.AbstractService;
import gleam.exception.ServerStarupError;
import gleam.util.ClazzUtil;

/**
 * 控制台
 * 
 * @author redback
 * @version 1.00
 * @time 2020-4-26 15:02
 */
public class ConsoleManager extends AbstractService {

	private static ConsoleManager instance = new ConsoleManager();

	// TODO 分隔符规则待定
	private static final String CMD_SPLITTER = "#";

	public static ConsoleManager getInstance() {
		return instance;
	}

	/** CmdType -> cmdHandler */
	private final Map<String, ConsoleCmdHandler> cmd2Handlers = new HashMap<>();

	@Override
	protected void onInitialize() throws Exception {
		scanCmdHandlers();
	}

	/**
	 * 处理平台命令
	 * 
	 * @param cmd example : cmdType#cmdContent
	 * @return result
	 */
	public String handlerConsoleCmd(String cmd) {

		logger.debug("处理后台请求请求 :\n {}", cmd);

		String realCmd;

		// 命令预处理，判空，掐头去尾
		if (StringUtils.isEmpty(cmd) || StringUtils.isEmpty((realCmd = cmd.trim()))) {
			return ConsoleErrorMsg.CMD_EMPTY.getResponse();
		}
		int splitterIndex = realCmd.indexOf(CMD_SPLITTER);
		String cmdType = realCmd;
		String cmdContent = "";
		if (splitterIndex > 0) {
			cmdType = realCmd.substring(0, splitterIndex);
			cmdContent = realCmd.substring(splitterIndex + 1, realCmd.length());
		}
		// 命令类型
		ConsoleCmdHandler consoleCmdHandler = cmd2Handlers.get(cmdType);
		if (consoleCmdHandler == null) {
			logger.error("can't handler cmd : [{}], because no found cmdType [{}]", cmd, cmdType);
			return String.format(ConsoleErrorMsg.COM_CANNOT_HANDLER.getResponse(), cmdType);
		}

		String response;
		try {
			response = consoleCmdHandler.handler(cmdContent);
		} catch (Exception e) {
			logger.error("handler cmd [{}] exception", cmd, e);
			response = ConsoleErrorMsg.INNER_ERROR.getResponse();
		}
		return StringUtils.isEmpty(response) ? ConsoleErrorMsg.SUCCESS.getResponse() : response;
	}

	/**
	 * 注册命令处理器
	 * 
	 * @param consoleCmdHandler
	 */
	private void registerCmdHandler(ConsoleCmdHandler consoleCmdHandler) {
		String cmdType = consoleCmdHandler.getCmdType();
		ConsoleCmdHandler oldCmdHandler = cmd2Handlers.put(cmdType, consoleCmdHandler);
		if (oldCmdHandler != null) { // 如果处理器重复，直接抛出异常
			logger.error("Console Cmd handler [{}] repeated ， old handler ： [{}],  new handler : [{}]", cmdType,
					oldCmdHandler.getClass().getName(), consoleCmdHandler.getClass().getName());
			throw new ServerStarupError("console cmd handler register repeated ! ");
		}
		logger.info("------>consoleCmdHandler type:{},consoleCmdHandler name:{}", cmdType,
				consoleCmdHandler.getClass().getSimpleName());
	}

	// 扫描接口实现类
	private void scanCmdHandlers()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		// 扫描 foundation包
		List<ConsoleCmdHandler> baseCmdHandlers = ClazzUtil.scanImplAndNewInstances(ClazzUtil.FOUNDATION_PACKAGE_NAME,
				ConsoleCmdHandler.class);
		for (ConsoleCmdHandler consoleCmdHandler : baseCmdHandlers) {
			registerCmdHandler(consoleCmdHandler);
		}
		logger.info("------>consoleCmdHandler foundation size:{}", baseCmdHandlers.size());

		// 扫描 game.com
		List<ConsoleCmdHandler> gameCmdHandlers = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME,
				ConsoleCmdHandler.class);
		for (ConsoleCmdHandler consoleCmdHandler : gameCmdHandlers) {
			registerCmdHandler(consoleCmdHandler);
		}
		logger.info("------>consoleCmdHandler game size:{}", baseCmdHandlers.size());
	}

	public Map<String, ConsoleCmdHandler> getCmd2Handlers() {
		return cmd2Handlers;
	}

}
