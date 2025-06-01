package gleam.communication.inner.protocol;

import java.io.IOException;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 内网连接心跳包<br>
 * c->s
 * 
 * @author hdh
 *
 */
public class ReqInnerHeartbeat extends AbstractProtocol {

    public final static int ID = 901102;

    @Override
    public void decode(ByteBuf buffer) throws IOException {
    }

    @Override
    public void encode(ByteBuf buffer) {
    }

    @Override
    public int getId() {
        return ID;
    }

}
