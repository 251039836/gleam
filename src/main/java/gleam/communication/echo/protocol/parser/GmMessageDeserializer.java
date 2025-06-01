package gleam.communication.echo.protocol.parser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import gleam.communication.echo.protocol.GmMessage;

/**
 * 自定义反序列化 GmMessage 结构参考 "{"protocol":1001, "content": {xxx}}"
 * 
 * @author redback
 * @version 1.00
 * @time 2020-5-9 14:37
 */

public class GmMessageDeserializer extends JsonDeserializer<GmMessage> {

    private static final String PROTOCOL = "protocol";
    private static final String CONTENT = "content";

    @Override
    public GmMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        GmMessage gmMessage = new GmMessage();
        JsonNode jsonNode = p.getCodec().readTree(p);
        int id = jsonNode.get(PROTOCOL).asInt();
        String content = jsonNode.get(CONTENT).toString();
        gmMessage.setProtocol(id);
        gmMessage.setContent(content);
        return gmMessage;
    }
}
