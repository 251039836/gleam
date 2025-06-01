package gleam.communication.http.controller;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;
import gleam.util.time.DateFormatUtils;

class HtmlOutput {

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
			} catch (Exception ex) {
				ex.printStackTrace();
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
