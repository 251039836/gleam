package gleam.communication.http.controller;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.annotation.Param;
import gleam.communication.http.annotation.RequestMapping;
import gleam.communication.http.dispatcher.DispatcherServlet;
import gleam.communication.http.helper.HttpServerHelper;
import gleam.config.ServerSettings;
import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;
import gleam.util.script.ScriptUtil;
import gleam.util.time.DateFormatUtils;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 网页debug<br>
 * 主要用于查询内存数据 但也可以写一些逻辑执行<br>
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
		addExample("查询玩家对象(角色名)", "com.game.module.player.PlayerManager.getInstance().getPlayerByName(\"玩家角色名\");");
		addExample("查询游戏服上下文", "com.game.context.GameContext.getInstance();");
		addExample("查看ObjectPool", "gleam.util.pool.ObjectPool.printStatistics();");
		addExample("查询玩家缓存", "com.game.module.player.PlayerManager.getInstance().getPlayerCache();");
		addExample("查询配置数据", "gleam.config.ConfigManager.getInstance().getContainers();");
	}

	private static void addExample(String name, String code) {
		SHELL_EXAMPLES.put(name, code);
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
		try {
			Object rootObj = ScriptUtil.executeCode(code, null);
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
							logger.error("code[{}] depth[{}] obj[{}] field[{}] getValue error.", code, depth,
									showObj.getClass().getSimpleName(), field, e);
						}
					}
				}
				responseWrite(resp, "<b>" + showObj.getClass().getSimpleName() + "</b><br/>");
				String objJson = toJsonHtml(showObj, code, depth);
				responseWrite(resp, objJson);
			}
			responseWrite(resp, "</pre></body>");
		} catch (ScriptException e) {
			logger.error("shell execute code[{}] depth[{}] error.", code, depth, e);
			responseWrite(resp, e.getMessage());
		}
	}

	/**
	 * 初始页面
	 * 
	 * @param resp
	 */
	private void showInitHtml(FullHttpResponse resp) {
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		responseWrite(resp, "<body>");
		responseWrite(resp, "<form action='#' method='POST' >");
		responseWrite(resp, "NewBee/Huashen@youai之遗  shell调试器 v1.2 (输入调试指令)：</br>");
		responseWrite(resp, "<textarea type='text' rows='10' style='width:100%' name='code'></textarea></br>");
		responseWrite(resp, "<input type='submit' value=' 执行 ' >");
		responseWrite(resp, "</form>");
		responseWrite(resp, "<div>调试指令 Examples:</div>");
		responseWrite(resp, "<table border='3'>");
		responseWrite(resp, "<tr>");
		responseWrite(resp, "<th>name</th>");
		responseWrite(resp, "<th>code</th>");
		responseWrite(resp, "</tr>");
		for (Entry<String, String> entry : SHELL_EXAMPLES.entrySet()) {
			responseWrite(resp, "<tr>");
			responseWrite(resp, "<td>" + entry.getKey() + "</td>");
			String code = entry.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("<td><a target='_blank' href='shell?code=")//
					.append(URLEncoder.encode(code, StandardCharsets.UTF_8))//
					.append("'>").append(code).append("</a></td>");
			responseWrite(resp, sb.toString());
		}
		responseWrite(resp, "</div>");
		responseWrite(resp, "</table>");
		responseWrite(resp, "<div>可在" + getClass().getName() + "处添加常用方法</div>");
		responseWrite(resp, "</body>");
	}

	private void responseWrite(FullHttpResponse resp, String str) {
		resp.content().writeCharSequence(str, StandardCharsets.UTF_8);
	}

	static String toJsonHtml(Object object, String code, String depth) {
		if (object == null) {
			return "null";
		}
		if (object.getClass() == null) {
			return "\"class Null\"";
		} else if (object instanceof Enum e) {
			return '"' + e.name() + '"';
		} else if (ClazzUtil.isBaseType(object)) {
			return String.valueOf(object);
		} else {
			try {
				Map<String, Object> fieldValues = ClazzUtil.getFieldValueMap(object);
				String json = toJson(fieldValues, code, depth);
				return JsonUtil.formatJson(json);
			} catch (Exception e) {
				logger.error("[{}] depth[{}] toJsonHtml fail.", code, depth, e);
				return "\"Error\"";
			}
		}

	}

	/**
	 * @return a JSON object representation for a map
	 */
	private static String toJson(Map<String, Object> fieldValues, String code, String depth) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for (Entry<String, Object> entry : fieldValues.entrySet()) {
			String k = entry.getKey();
			Object v = entry.getValue();
			if (k == null) {
				throw new IllegalArgumentException("Null key for a Map not allowed");
			}
			if (!first) {
				sb.append(",");
			}
			sb.append('"').append(k).append('"').append(':');
			sb.append(fieldValue2HtmlString(k, v, code, depth));
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * 参数值转为显示的字符串
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @param code
	 * @param depth
	 * @return
	 */
	private static String fieldValue2HtmlString(String fieldName, Object fieldValue, String code, String depth) {
		if (fieldValue == null) {
			return "null";
		}
		if (fieldValue instanceof Boolean b) {
			return b.toString();
		} else if (fieldValue instanceof Number n) {
			if (fieldValue instanceof Double d) {
				if (d.isInfinite() || d.isNaN()) {
					throw new RuntimeException(
							"Number ${n} can't be serialized as JSON: infinite or NaN are not allowed in JSON");
				}
			}
			if (fieldValue instanceof Float f) {
				if (f.isInfinite() || f.isNaN()) {
					throw new RuntimeException(
							"Number ${n} can't be serialized as JSON: infinite or NaN are not allowed in JSON");
				}
			}
			return n.toString();
		}
		StringBuffer sb = new StringBuffer();
		sb.append('"');
		if (fieldValue instanceof Character c) {
			sb.append(c);
		} else if (fieldValue instanceof String s) {
			sb.append(StringEscapeUtils.escapeJava(s));
		} else if (fieldValue instanceof Date d) {
			sb.append(DateFormatUtils.format(d));
		} else if (fieldValue instanceof Calendar c) {
			sb.append(DateFormatUtils.format(c.getTime()));
		} else if (fieldValue instanceof UUID u) {
			sb.append(u.toString());
		} else if (fieldValue instanceof URL u) {
			sb.append(u.toString());
		} else if (ClazzUtil.isBaseType(fieldValue)) {
			sb.append(String.valueOf(fieldValue));
		} else {
			// 其他结构体
			String depthValue = fieldName;
			if (!StringUtils.isBlank(depth)) {
				depthValue = depth + "." + fieldName;
			}
			sb.append("<a target='_blank' href='shell?code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8))//
					.append("&depth=").append(URLEncoder.encode(depthValue, StandardCharsets.UTF_8))//
					.append("'>").append(fieldValue.getClass().getSimpleName()).append("</a>");
		}
		sb.append('"');
		return sb.toString();
	}
}
