package gleam.communication.protocol;

import java.io.IOException;

import gleam.communication.Protocol;
import gleam.exception.UnsupportedMethodException;
import io.netty.buffer.ByteBuf;

/**
 * 已编码的协议<br>
 * 只适用于发送端<br>
 * 可用于重复发送/广播相同内容的协议<br>
 * 用于重复发送/广播时 seq应当=0
 * 
 * @author hdh
 *
 */
public class EncodedProtocol extends AbstractProtocol {

	/**
	 * 协议id
	 */
	private int id;
	/**
	 * 协议已编译的内容
	 */
	private byte[] data;

	public EncodedProtocol() {
	}

	public EncodedProtocol(int id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	public EncodedProtocol(int id, int seq, byte[] data) {
		this.id = id;
		this.seq = seq;
		this.data = data;
	}

	public EncodedProtocol(Protocol protocol) {
		this.id = protocol.getId();
		this.data = ProtocolUtil.encodeMessage(protocol);
	}

	@Override
	public void decode(ByteBuf buffer) throws IOException {
		throw new UnsupportedMethodException();
	}

	@Override
	public void encode(ByteBuf buffer) {
		buffer.writeBytes(data);
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setId(int id) {
		this.id = id;
	}

}
