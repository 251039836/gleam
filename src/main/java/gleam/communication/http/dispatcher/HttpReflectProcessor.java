package gleam.communication.http.dispatcher;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.annotation.Param;
import gleam.communication.http.annotation.RequestMapping;
import gleam.communication.http.helper.HttpServerHelper;
import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;
import gleam.util.reflect.MethodInvoker;
import gleam.util.reflect.MethodParam;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * http请求处理器<br>
 * 通过反射实现<br>
 * 相关注解 {@link RequestMapping} {@link Param}
 * 
 * @author hdh
 */
public class HttpReflectProcessor implements HttpProcessor {
	private static final Logger logger = LoggerFactory.getLogger(HttpReflectProcessor.class);

	public static HttpReflectProcessor build(String url, Object controller, Method method) {
		MethodInvoker methodInvoker = MethodInvoker.create(controller, method);
		List<MethodParam> methodParams = null;
		Parameter[] params = method.getParameters();
		// 获取到注解
		for (Parameter parameter : params) {
			// 默认使用参数名字
			String paramName = parameter.getName();
			Param param = parameter.getAnnotation(Param.class);
			if (param != null) {
				// 如果有注解名, 以注解名为准
				paramName = param.value();
			}
			Class<?> paramType = parameter.getType();
			MethodParam methodParam = MethodParam.create(paramName, paramType);
			if (methodParams == null) {
				methodParams = new ArrayList<>();
			}
			methodParams.add(methodParam);
		}
		HttpReflectProcessor processor = new HttpReflectProcessor();
		processor.setUrl(url);
		processor.setInvoker(methodInvoker);
		processor.setMethodParams(methodParams);
		return processor;
	}

	private String url;

	private MethodInvoker invoker;

	private List<MethodParam> methodParams;

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public FullHttpResponse processRequest(Channel channel, FullHttpRequest request) throws IOException {
		try {
			Object[] args = parseRequestParams(request);
			Object result = invoker.invoke(args);
			FullHttpResponse response = HttpServerHelper.createSuccessResponse(result);
			return response;
		} catch (Exception e) {
			logger.error("DefaultHttpDispatcher handler url[{}] error", url, e);
			return HttpServerHelper.createResponse(HttpResponseStatus.BAD_REQUEST);
		}
	}

	private Object[] parseRequestParams(FullHttpRequest request) throws IOException {
		if (methodParams == null || methodParams.size() == 0) {
			return new Object[0];
		}
		int size = methodParams.size();
		Map<String, Object> paramMap = HttpServerHelper.parseParamMap(request);
		Object[] args = new Object[size];
		for (int i = 0; i < methodParams.size(); i++) {
			MethodParam param = methodParams.get(i);
			String paramName = param.getName();
			Class<?> paramType = param.getParamType();
			if (paramType.isAssignableFrom(Map.class)) {
				// 若是map类型, 直接把所有参数丢进去
				args[i] = paramMap;
			} else {
				Object paramValue = paramMap.get(paramName);
				// 基础数据类型
				if (paramValue != null) {
					if (ClazzUtil.isBaseType(paramType)) {
						args[i] = paramType.cast(paramValue);
					} else {
						// 自定义结构体
						args[i] = JsonUtil.toObject(String.valueOf(paramValue), paramType);
					}
				}
			}
		}
		return args;
	}

	public MethodInvoker getInvoker() {
		return invoker;
	}

	public void setInvoker(MethodInvoker invoker) {
		this.invoker = invoker;
	}

	public List<MethodParam> getMethodParams() {
		return methodParams;
	}

	public void setMethodParams(List<MethodParam> methodParams) {
		this.methodParams = methodParams;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
