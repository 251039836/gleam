package gleam.communication.echo.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gleam.communication.echo.protocol.parser.GmMessageDeserializer;

/**
 *
 * 后台 GM 协议消息, 结构参考 "{"protocol":1001, "content": {xxx}}"
 *
 * @author redback
 * @version 1.00
 * @time 2020-5-9 14:31
 */

@JsonDeserialize(using = GmMessageDeserializer.class)
public class GmMessage {

    /**
     * 协议 id
     */
    private int protocol;
    /**
     * 协议内容
     */
    private String content;

    public String getContent() {
        return content;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

}
