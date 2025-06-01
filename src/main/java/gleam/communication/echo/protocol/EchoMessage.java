package gleam.communication.echo.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

public class EchoMessage extends AbstractProtocol {

	// FixME ID 规则待确定
	public static final int ID = 204001;

	public static EchoMessage valueOf(String content) {
		EchoMessage echoMessage = new EchoMessage();
		echoMessage.setContent(content);
		return echoMessage;
	}

	private String content;

	@Override
	public void decode(ByteBuf buffer) throws IOException {
		// FIXME length?
		int readableBytes = buffer.readableBytes();
		byte[] bytes = new byte[readableBytes];
		buffer.readBytes(bytes);
		content = new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void encode(ByteBuf buffer) {
		// FIXME length?
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		buffer.writeBytes(bytes);
	}

	public String getContent() {
		return content;
	}

	@Override
	public int getId() {
		return ID;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
