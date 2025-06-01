package gleam.communication.http.dispatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import gleam.communication.http.helper.HttpServerHelper;
import gleam.console.ConsoleCmdHandler;
import gleam.util.ClazzUtil;
import gleam.util.json.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpCmdProcessor implements HttpProcessor {

	private ConsoleCmdHandler cmdHandler;

	public HttpCmdProcessor(ConsoleCmdHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}

	@Override
	public String getUrl() {
		return getCmdHandler().getCmdType();
	}

	@Override
	public FullHttpResponse processRequest(Channel channel, FullHttpRequest request) throws IOException {
		Map<String, Object> paramMap = HttpServerHelper.parseParamMap(request);
		String cmd = "";
		if (paramMap.containsKey("cmd")) {
			cmd = (String) paramMap.get("cmd");
		}
		String result = cmdHandler.handler(cmd);
		FullHttpResponse response = HttpServerHelper.createResponse(HttpResponseStatus.OK);
		if (result != null) {
			String ret = ClazzUtil.isBaseType(result) ? String.valueOf(result) : JsonUtil.toJson(result);
			response.content().writeCharSequence(ret, StandardCharsets.UTF_8);
		}
		return response;
	}

	public ConsoleCmdHandler getCmdHandler() {
		return cmdHandler;
	}

	public void setCmdHandler(ConsoleCmdHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}

}
