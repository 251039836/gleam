package gleam.core.ref.protocol;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 转发目标实体的返回消息
 * 
 * @author hdh
 *
 */
public class ResEntityForward extends AbstractProtocol {

	public final static int ID = 902102;
	/**
	 * 来源服务器id
	 */
	private int srcServerType;
	/**
	 * 来源服务器id
	 */
	private int srcServerId;
	/**
	 * 目标服务器类型
	 */
	private int dstServerType;
	/**
	 * 目标服务器id
	 */
	private int dstServerId;
	/**
	 * 目标实体类型
	 */
	private int dstEntityType;
	/**
	 * 目标实体id
	 */
	private long dstEntityId;

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
		dstEntityType = buffer.readInt();
		dstEntityId = buffer.readLong();
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
		buffer.writeInt(dstEntityType);
		buffer.writeLong(dstEntityId);
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

	public int getDstEntityType() {
		return dstEntityType;
	}

	public void setDstEntityType(int dstEntityType) {
		this.dstEntityType = dstEntityType;
	}

	public long getDstEntityId() {
		return dstEntityId;
	}

	public void setDstEntityId(long dstEntityId) {
		this.dstEntityId = dstEntityId;
	}

}
