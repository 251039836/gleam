package gleam.communication.inner.protocol;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import gleam.communication.protocol.AbstractProtocol;

/**
 * 批量转发协议
 * 
 * @author lijr
 */
public class ReqMultipleServerForward extends AbstractProtocol {

	public final static int ID = 901121;

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
	 * 需要转发的服务器列表
	 */
	private List<Integer> dstServerIds = new ArrayList<>();
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
		short serverIdSize = buffer.readShort();
		if (serverIdSize > 0) {
			if (dstServerIds == null) {
				dstServerIds = new ArrayList<>();
			}
			for (short s = 0; s < serverIdSize; s++) {
				int serverId = buffer.readInt();
				dstServerIds.add(serverId);
			}
		}
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
		if (dstServerIds == null || dstServerIds.isEmpty()) {
			buffer.writeShort(0);
		} else {
			buffer.writeShort(dstServerIds.size());
			for (int serverId : dstServerIds) {
				buffer.writeInt(serverId);
			}
		}
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

	public List<Integer> getDstServerIds() {
		return dstServerIds;
	}

	public void setDstServerIds(List<Integer> dstServerIds) {
		this.dstServerIds = dstServerIds;
	}

	public int getForwardMsgId() {
		return forwardMsgId;
	}

	public void setForwardMsgId(int forwardMsgId) {
		this.forwardMsgId = forwardMsgId;
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
