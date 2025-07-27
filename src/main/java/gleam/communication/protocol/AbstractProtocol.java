package gleam.communication.protocol;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import io.netty.buffer.ByteBuf;

public abstract class AbstractProtocol implements Protocol {
	/**
	 * 收到该协议的链接
	 */
	protected transient Connection connection;
	/**
	 * 消息序号<br>
	 * 客户端的序号自增<br>
	 * 服务端返回的序号与请求序号相同<br>
	 * 服务端主动发送的协议 序号为0<br>
	 * 只在最外层的编译时进行读写
	 */
	protected int seq;

	@Override
	public int getSeq() {
		return seq;
	}

	protected String readString(ByteBuf buffer) {
		short length = buffer.readShort();
		if (length <= 0) {
			return StringUtils.EMPTY;
		}
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void setSeq(int seq) {
		this.seq = seq;
	}

	protected void writeString(ByteBuf buffer, String str) {
		if (str == null || str.isEmpty()) {
			buffer.writeShort(0);
			return;
		}
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		buffer.writeShort(bytes.length);
		buffer.writeBytes(bytes);
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
