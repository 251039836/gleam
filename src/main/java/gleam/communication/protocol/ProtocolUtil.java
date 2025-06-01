package gleam.communication.protocol;

import java.io.IOException;

import gleam.communication.Protocol;
import gleam.communication.protocol.factory.ProtocolFactory;
import gleam.util.compress.impl.GzipCompressor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ProtocolUtil {

	/**
	 * 解析协议
	 * 
	 * @param msgId
	 * @param msgSeq
	 * @param msgData
	 * @return
	 * @throws Exception
	 */
	public static Protocol decodeMessage(int msgId, int msgSeq, byte[] msgData) throws Exception {
		Protocol protocol = ProtocolFactory.getProtocol(msgId);
		if (protocol == null) {
			return null;
		}
		protocol.setSeq(msgSeq);
		if (msgData != null && msgData.length > 0) {
			ByteBuf tmpBuffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(msgData.length);
			tmpBuffer.writeBytes(msgData);
			protocol.decode(tmpBuffer);
		}
		return protocol;
	}

	/**
	 * 将协议体编码转为字节流<br>
	 * 不含协议id 协议seq
	 * 
	 * @param msg
	 * @return
	 */
	public static byte[] encodeMessage(Protocol msg) {
		ByteBuf tmpBuffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
		msg.encode(tmpBuffer);
		byte[] msgData = new byte[tmpBuffer.readableBytes()];
		tmpBuffer.readBytes(msgData);
		return msgData;
	}

	public static Protocol compressProtocol(Protocol protocol) throws IOException {
		ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
		protocol.encode(buffer);
		int length = buffer.readableBytes();
		byte[] protoData = new byte[length];
		buffer.readBytes(protoData);
		buffer.clear();
		byte[] compressBytes = GzipCompressor.getInstance().compress(protoData);
		EncodedProtocol compressProtocol = new EncodedProtocol();
		compressProtocol.setId(protocol.getId());
		compressProtocol.setData(compressBytes);
		return compressProtocol;
	}
}
