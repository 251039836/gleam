package gleam.util.serverid.parser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import gleam.util.annotation.AutoRegister;
import gleam.util.json.AbstractCustomJsonParsers;
import gleam.util.json.JsonUtil;
import gleam.util.serverid.ServerIdGroup;

@AutoRegister
public class ServerIdGroupParser extends AbstractCustomJsonParsers<ServerIdGroup> {

	@Override
	public ServerIdGroup deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken currentToken = jp.getCurrentToken();
		if (currentToken == JsonToken.START_ARRAY) {
			return parseToServerIdGroup(jp);
		}
		return defaultDeserializer.deserialize(jp, ctxt);
	}

	private ServerIdGroup parseToServerIdGroup(JsonParser jp) throws IOException {
		JsonNode jsonNode = jp.readValueAsTree();
		ServerIdGroup serverIdGroup = new ServerIdGroup();
		int[][] arr = JsonUtil.toObject(jsonNode, int[][].class);
		serverIdGroup.setServerIds(arr);
		return serverIdGroup;
	}

}
