package gleam.core.ref.protocol;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 返回实体检测结果
 * 
 * @author hdh
 *
 */
public class ResEntityCheck extends AbstractProtocol {

	public final static int ID = 902202;
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
	 * 是否存在
	 */
	private boolean dstEntityExists;

	@Override
	public void decode(ByteBuf buffer) throws Exception {
		srcServerType = buffer.readInt();
		srcServerId = buffer.readInt();
		dstServerType = buffer.readInt();
		dstServerId = buffer.readInt();
		dstEntityType = buffer.readInt();
		dstEntityId = buffer.readLong();
		dstEntityExists = buffer.readBoolean();
	}

	@Override
	public void encode(ByteBuf buffer) {
		buffer.writeInt(srcServerType);
		buffer.writeInt(srcServerId);
		buffer.writeInt(dstServerType);
		buffer.writeInt(dstServerId);
		buffer.writeInt(dstEntityType);
		buffer.writeLong(dstEntityId);
		buffer.writeBoolean(dstEntityExists);
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

	public boolean isDstEntityExists() {
		return dstEntityExists;
	}

	public void setDstEntityExists(boolean dstEntityExists) {
		this.dstEntityExists = dstEntityExists;
	}

}
