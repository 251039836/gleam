package gleam.communication.inner.protocol;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 内网rpc转发协议<br>
 * 将要发送的rpc协议 封装到此协议之中<br>
 * 发送服务器 通过中间节点 发送给目标服务器 并等待目标服务器的返回协议<br>
 * 中间节点将该包装协议完整的转发到目标服务器 在目标服务器中拆包<br>
 * 
 * <pre>
 * client1
 * ||
 * ||ReqInnerRpcForward(ForwardMsg)
 * \/
 * server
 * ||
 * ||ReqInnerRpcForward(ForwardMsg)
 * \/
 * client2
 * </pre>
 * 
 * @author hdh
 *
 */
public class ReqInnerRpcForward extends AbstractProtocol {

	public final static int ID = 901112;

	/**
	 * 来源服务器类型
	 */
	private int srcServerType;
	/**
	 * 来源服务器id
	 */
	private int srcServerId;
	/**
	 * 客户端的服务器类型
	 */
	private int dstServerType;
	/**
	 * 目标服务器id
	 */
	private int dstServerId;
	/**
	 * 转发的消息的seq
	 */
	private int forwardMsgSeq;
	/**
	 * 转发的消息id
	 */
	private int forwardMsgId;
	/**
	 * 转发的消息体
	 */
	private byte[] forwardMsgData;

	@Override
	public void decode(ByteBuf buffer) throws Exception {
		srcServerType = buffer.readInt();
		srcServerId = buffer.readInt();
		dstServerType = buffer.readInt();
		dstServerId = buffer.readInt();
		forwardMsgSeq = buffer.readInt();
		forwardMsgId = buffer.readInt();
		int forwardMsgDataLength = buffer.readInt();
		forwardMsgData = new byte[forwardMsgDataLength];
		buffer.readBytes(forwardMsgData);
	}

	@Override
	public void encode(ByteBuf buffer) {
		buffer.writeInt(srcServerType);
		buffer.writeInt(srcServerId);
		buffer.writeInt(dstServerType);
		buffer.writeInt(dstServerId);
		buffer.writeInt(forwardMsgSeq);
		buffer.writeInt(forwardMsgId);
		if (forwardMsgData != null) {
			buffer.writeInt(forwardMsgData.length);
			buffer.writeBytes(forwardMsgData);
		} else {
			buffer.writeInt(0);
		}
	}

	@Override
	public int getId() {
		return ID;
	}

	public int getSrcServerId() {
		return srcServerId;
	}

	public void setSrcServerId(int srcServerId) {
		this.srcServerId = srcServerId;
	}

	public int getDstServerId() {
		return dstServerId;
	}

	public void setDstServerId(int dstServerId) {
		this.dstServerId = dstServerId;
	}

	public int getForwardMsgId() {
		return forwardMsgId;
	}

	public void setForwardMsgId(int forwardMsgId) {
		this.forwardMsgId = forwardMsgId;
	}

	public int getForwardMsgSeq() {
		return forwardMsgSeq;
	}

	public void setForwardMsgSeq(int forwardMsgSeq) {
		this.forwardMsgSeq = forwardMsgSeq;
	}

	public byte[] getForwardMsgData() {
		return forwardMsgData;
	}

	public void setForwardMsgData(byte[] forwardMsgData) {
		this.forwardMsgData = forwardMsgData;
	}

	public int getSrcServerType() {
		return srcServerType;
	}

	public void setSrcServerType(int srcServerType) {
		this.srcServerType = srcServerType;
	}

	public int getDstServerType() {
		return dstServerType;
	}

	public void setDstServerType(int dstServerType) {
		this.dstServerType = dstServerType;
	}

}
