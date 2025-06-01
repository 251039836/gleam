package gleam.communication.http.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;

import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class HttpServerHelper {
	/**
	 * 解析参数<br>
	 * 
	 * @note 这里只支持了get, post两种类型的http请求.
	 * @param httpRequest
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Object> parseParamMap(FullHttpRequest httpRequest) throws IOException {
		HttpMethod method = httpRequest.method();
		Map<String, Object> paramMap = new HashMap<>();
		if (HttpMethod.GET == method) {
			// 是GET请求
			QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
			Map<String, List<String>> parameters = decoder.parameters();
			for (Entry<String, List<String>> entry : parameters.entrySet()) {
				paramMap.put(entry.getKey(), entry.getValue().get(0)); // value取第一个
			}
		} else if (HttpMethod.POST == method) {
			// 是POST请求
			// Json内容
			HttpHeaders headers = httpRequest.headers();
			if (headers.containsValue(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, true)) {
				if (httpRequest.content().isReadable()) {
					String text = httpRequest.content().toString(StandardCharsets.UTF_8);
					Map<String, Object> map = JsonUtil.toObject(text, new TypeReference<Map<String, Object>>() {
					});
					paramMap.putAll(map);
				}
			} else
//				if (headers.containsValue(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.MULTIPART_FORM_DATA, true))
			{
				// 内容在data
				HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false),
						httpRequest);
				List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
				for (InterfaceHttpData parm : parmList) {
					if (parm.getHttpDataType() == HttpDataType.Attribute) {
						Attribute data = (Attribute) parm;
						paramMap.put(data.getName(), data.getValue());
					}
				}
			}
		}
		return paramMap;
	}

	/**
	 * 创建一个HttpResponse
	 * 
	 * @param status
	 * @return
	 */
	public static FullHttpResponse createResponse(HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		return response;
	}

	public static FullHttpResponse createSuccessResponse(Object result) {
		if (result == null) {
			return createResponse(HttpResponseStatus.OK);
		}
		if (result instanceof FullHttpResponse) {
			return (FullHttpResponse) result;
		}
		FullHttpResponse response = HttpServerHelper.createResponse(HttpResponseStatus.OK);
		String ret = ClazzUtil.isBaseType(result) ? String.valueOf(result) : JsonUtil.toJson(result);
		response.content().writeCharSequence(ret, StandardCharsets.UTF_8);
		return response;
	}

}
