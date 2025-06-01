package gleam.communication.http.dispatcher;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.annotation.RequestMapping;
import gleam.communication.http.helper.HttpServerHelper;
import gleam.console.ConsoleCmdHandler;
import gleam.console.ConsoleManager;
import gleam.util.ClazzUtil;
import gleam.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DispatcherServlet {
	private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

	private final static String SLASH = "/";
	private final Map<String, HttpProcessor> processors = new HashMap<>();

	private final static String[] WHITE_IP_LIST = { "127.0.0.1" };

	public void init() {
		//
		registerControllers();
		// 注册gm命令&http操作命令
		registerCmdHandlers();
	}

	private void registerCmdHandlers() {
		Map<String, ConsoleCmdHandler> cmd2Handlers = ConsoleManager.getInstance().getCmd2Handlers();
		for (ConsoleCmdHandler cmdHandler : cmd2Handlers.values()) {
			HttpCmdProcessor processor = new HttpCmdProcessor(cmdHandler);
			registerProcessor(processor);
		}
	}

	private void registerControllers() {
		List<Class<?>> foundationControllers = ClazzUtil.scanClassList(ClazzUtil.FOUNDATION_PACKAGE_NAME, (clazz) -> {
			RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
			return requestMapping != null;
		});
		if (foundationControllers != null && !foundationControllers.isEmpty()) {
			for (Class<?> clazz : foundationControllers) {
				registerController(clazz);
			}
		}
		List<Class<?>> gameControllers = ClazzUtil.scanClassList(ClazzUtil.GAME_PACKAGE_NAME, (clazz) -> {
			RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
			return requestMapping != null;
		});
		if (gameControllers != null && !gameControllers.isEmpty()) {
			for (Class<?> clazz : gameControllers) {
				registerController(clazz);
			}
		}
	}

	private void registerController(Class<?> clazz) {
		RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
		if (mapping == null) {
			return;
		}
		String clazzName = clazzName(clazz, mapping);
		try {
			// 只有带有RequestMethod的才视为对外api
			Method[] methods = clazz.getDeclaredMethods();
			Object controller = clazz.getDeclaredConstructor().newInstance();
			for (Method method : methods) {
				RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
				if (methodMapping == null) {
					continue;
				}
				String methodName = methodName(method, methodMapping);
				String requestUrl = clazzName.concat(methodName);
				HttpReflectProcessor requester = HttpReflectProcessor.build(requestUrl, controller, method);
				registerProcessor(requester);
			}
		} catch (Exception e) {
			logger.error("register class[{}] error", clazz.getSimpleName(), e);
		}
	}

	private void registerProcessor(HttpProcessor processor) {
		String url = processor.getUrl();
		HttpProcessor other = processors.putIfAbsent(url, processor);
		if (other != null) {
			logger.error("same url[{}] register.", url);
		}
	}

	public FullHttpResponse processRequest(Channel channel, FullHttpRequest request) throws URISyntaxException {
		long begin = System.currentTimeMillis();
		URI uri = new URI(request.uri());
		String url = uri.getPath();// 获取到请求uri
		SocketAddress remoteAddress = channel.remoteAddress();
		FullHttpResponse response = null;
		if (!checkAddress(remoteAddress)) {
			response = HttpServerHelper.createResponse(HttpResponseStatus.UNAUTHORIZED);
			return response;
		}
		HttpProcessor httpProcessor = processors.get(url);
		if (httpProcessor == null) {
			response = HttpServerHelper.createResponse(HttpResponseStatus.NOT_FOUND);
			return response;
		}
		boolean success = false;
		try {
			response = httpProcessor.processRequest(channel, request);
			long used = System.currentTimeMillis() - begin;
			// 协议处理超过1秒
			if (used > 1000) {
				logger.warn("协议[{}]处理慢!!!耗时{}ms", url, used);
			}
			success = true;
		} catch (Exception e) {
			response = HttpServerHelper.createResponse(HttpResponseStatus.BAD_REQUEST);
			logger.error("DefaultHttpDispatcher handler url[{}] error", url, e);
		}
		logger.info("ip[{}] processRequest {}.url[{}]", remoteAddress, (success ? "success" : "fail"), url);
		return response;
	}

	private boolean checkAddress(SocketAddress remoteAddress) {
		String ip = null;
		if (remoteAddress instanceof InetSocketAddress socketAddress) {
			ip = socketAddress.getHostName();
		} else {
			ip = remoteAddress.toString();
		}
		return StringUtils.equalsAny(ip, WHITE_IP_LIST);
	}

	/**
	 * 获取映射地址, 也是方法名
	 * 
	 * @param method
	 * @param mapping
	 * @return
	 */
	private String methodName(Method method, RequestMapping mapping) {
		String methodName = mapping.value();
		if (methodName == null || methodName.equals("")) {
			methodName = method.getName();
		}
		methodName = methodName.startsWith(SLASH) ? methodName : SLASH.concat(methodName);
		return methodName;
	}

	/**
	 * 获取到映射地址,也是类名
	 * 
	 * @param clazz
	 * @param mapping
	 * @return
	 */
	private String clazzName(Class<?> clazz, RequestMapping mapping) {
		// 获取映射url
		String clazzName = mapping.value();
		if (clazzName == null || clazzName.equals("")) {
			// 未指定映射, 使用类名作为映射
			clazzName = clazz.getSimpleName();
			clazzName = StringUtil.firstCharLower(clazzName);
		}
		clazzName = clazzName.startsWith(SLASH) ? clazzName : SLASH.concat(clazzName);
		return clazzName;
	}

	public Map<String, HttpProcessor> getProcessors() {
		return processors;
	}

	public HttpProcessor getProcessor(String url) {
		return processors.get(url);
	}

}
