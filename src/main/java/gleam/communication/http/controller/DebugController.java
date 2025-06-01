package gleam.communication.http.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.annotation.Param;
import gleam.communication.http.annotation.RequestMapping;
import gleam.communication.http.dispatcher.DispatcherServlet;
import gleam.communication.http.helper.HttpServerHelper;
import gleam.config.ServerSettings;
import gleam.util.ClazzUtil;
import gleam.util.script.ScriptUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 网页debug<br>
 * 主要用于查询内存操作 但也可以写一些逻辑执行<br>
 * 此方法极其危险 默认只允许在debug模式下使用<br>
 * {@link DispatcherServlet#WHITE_IP_LIST}<br>
 * {@link DebugController#isDebug()}
 * 
 * @author hdh
 */
@RequestMapping("/debug")
public class DebugController {
	private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

	private static final Map<String, String> SHELL_EXAMPLES = new LinkedHashMap<String, String>();
	static {
		SHELL_EXAMPLES.put("查询玩家对象(角色名)",
				"com.game.module.player.PlayerManager.getInstance().getPlayerByName(\"玩家角色名\");");

		SHELL_EXAMPLES.put("查询在线玩家数量",
				"com.game.module.player.PlayerManager.getInstance().getPlayerCache().getOnlinePlayerSize();");

		SHELL_EXAMPLES.put("查询缓存玩家数量",
				"com.game.module.player.PlayerManager.getInstance().getPlayerCache().getCachePlayerMap().size();");

		SHELL_EXAMPLES.put("查询配置数据", "gleam.config.ConfigManager.getInstance().getContainers();");
	}

	/**
	 * 
	 * @param code
	 * @param depth
	 * @param response
	 */
	@RequestMapping("/shell")
	public FullHttpResponse shell(@Param("code") String code, @Param("depth") String depth) {
		if (!isDebug()) {
			FullHttpResponse response = HttpServerHelper.createResponse(HttpResponseStatus.UNAUTHORIZED);
			return response;
		}
		FullHttpResponse response = HttpServerHelper.createResponse(HttpResponseStatus.OK);
		if (StringUtils.isBlank(code)) {
			showInitHtml(response);
			return response;
		}
		logger.info("shell code[{}] depth[{}]", code, depth);
		showObjTree(response, code, depth);
		return response;
	}

	/**
	 * 是否测试环境
	 * 
	 * @return
	 */
	private boolean isDebug() {
		Properties properties = ServerSettings.getProperties();
		for (Object key : properties.keySet()) {
			String keyStr = String.valueOf(key);
			if (keyStr.contains("release")) {
				boolean release = ServerSettings.getBooleanProperty(keyStr, true);
				if (release) {
					return false;
				}
			}
		}
		return true;
	}

	private void showObjTree(FullHttpResponse resp, String code, String depth) {
//		resp.setCharacterEncoding("utf-8");
//		resp.setContentType("text/html");
		try {
			Object rootObj = ScriptUtil.executeCode(code, null);

//			responseWrite(resp, "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='zh-CN' dir='ltr'>");
//			responseWrite(resp, "<head>");
//			responseWrite(resp, "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
//			responseWrite(resp, "</head>");
			resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

			responseWrite(resp, "<body><pre>");

			responseWrite(resp, "<b>" + code + "</b><br/>");

			if (rootObj == null) {
				responseWrite(resp, "查询对象为空！");
			} else {
				responseWrite(resp, "<b>" + rootObj.getClass().getSimpleName());
				if (!StringUtils.isBlank(depth)) {
					responseWrite(resp, "." + depth);
				}
				responseWrite(resp, "</b><br/>");
				Object showObj = rootObj;
				if (depth != null) {
					for (String field : depth.split("\\.")) {
						if (StringUtils.isBlank(field)) {
							continue;
						}
						try {
							Map<String, Object> fieldMap = ClazzUtil.getFieldValueMap(showObj);
							showObj = fieldMap.get(field);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				responseWrite(resp, "<b>" + showObj.getClass().getSimpleName() + "</b><br/>");
				String objJson = HtmlOutput.toJsonHtml(showObj, code, depth);
				responseWrite(resp, objJson);
			}
			responseWrite(resp, "</pre></body>");
//			responseWrite(resp, "</html>");
		} catch (ScriptException e) {
			logger.error("shell execute code[{}] depth[{}] error.", code, depth, e);
			responseWrite(resp, e.getMessage());
		}
	}

	private void showInitHtml(FullHttpResponse resp) {
//		responseWrite(resp, "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='zh-CN' dir='ltr'>");
//		responseWrite(resp, "<head>");
//		responseWrite(resp, "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
//		responseWrite(resp, "</head>");
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

		responseWrite(resp, "<body>");
		responseWrite(resp, "<form action='#' method='POST' >");
		responseWrite(resp, "NewBee/msn@youai的遗产  shell调试器 v1.2 (输入调试指令)：</br>");
		responseWrite(resp, "<textarea type='text' rows='10' style='width:100%' name='code'></textarea></br>");
		responseWrite(resp, "<input type='submit' value=' 执行 ' >");
		responseWrite(resp, "</form>");
		responseWrite(resp, "<div>调试指令 Examples:</div>");
		responseWrite(resp, "<div>");
		responseWrite(resp, "<div>");
		for (Entry<String, String> entry : SHELL_EXAMPLES.entrySet()) {
			responseWrite(resp, "//" + entry.getKey() + "<br/>");
			String code = entry.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("<a target='_blank' href='shell?code=")//
					.append(URLEncoder.encode(code, StandardCharsets.UTF_8))//
					.append("'>").append(code).append("</a><br/>");
			responseWrite(resp, sb.toString());
		}

		responseWrite(resp, "</div>");
		responseWrite(resp, "</div>");
		responseWrite(resp, "</body>");
//		responseWrite(resp, "</html>");
	}

	private void responseWrite(FullHttpResponse resp, String str) {
		resp.content().writeCharSequence(str, StandardCharsets.UTF_8);

	}
}
