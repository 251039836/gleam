package gleam.communication.inner.protocol;

import java.io.IOException;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 返回内网身份验证<br>
 * s->c
 * 
 * @author hdh
 *
 */
public class ResInnerAuthenticate extends AbstractProtocol {

    public final static int ID = 901201;

    /**
     * 服务端的服务器类型
     */
    private int serverType;
    /**
     * 服务端的服务器id
     */
    private int serverId;

    @Override
    public void decode(ByteBuf buffer) throws IOException {
        serverType = buffer.readInt();
        serverId = buffer.readInt();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(serverType);
        buffer.writeInt(serverId);
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

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

}
