package gleam.communication.inner.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 请求内网身份验证<br>
 * c->s
 * 
 * @author hdh
 *
 */
public class ReqInnerAuthenticate extends AbstractProtocol {

    public final static int ID = 901101;

    /**
     * 客户端的服务器类型
     */
    private int serverType;
    /**
     * 客户端的服务器id<br>
     * 主服id
     */
    private int serverId;
    /**
     * 子服务器id列表<br>
     * 不含主服id
     */
    private List<Integer> childIds = new ArrayList<>();

    @Override
    public void decode(ByteBuf buffer) throws IOException {
        serverType = buffer.readInt();
        serverId = buffer.readInt();
        short childIdSize = buffer.readShort();
        if (childIdSize > 0) {
            if (childIds == null) {
                childIds = new ArrayList<>();
            }
            for (short s = 0; s < childIdSize; s++) {
                int childId = buffer.readInt();
                childIds.add(childId);
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(serverType);
        buffer.writeInt(serverId);
        if (childIds == null || childIds.isEmpty()) {
            buffer.writeShort(0);
        } else {
            buffer.writeShort(childIds.size());
            for (int childId : childIds) {
                buffer.writeInt(childId);
            }
        }
    }

    public List<Integer> getChildIds() {
        return childIds;
    }

    @Override
    public int getId() {
        return ID;
    }

    public int getServerId() {
        return serverId;
    }

    public int getServerType() {
        return serverType;
    }

    public void setChildIds(List<Integer> childIds) {
        this.childIds = childIds;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

}
