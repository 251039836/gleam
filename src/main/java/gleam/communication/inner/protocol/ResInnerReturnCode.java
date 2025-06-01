package gleam.communication.inner.protocol;

import java.io.IOException;

import gleam.communication.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;

/**
 * 内部返回消息 通用返回码 仅用于rpc请求的返回<br>
 * ask->answer
 * 
 * @author hdh
 *
 */
public class ResInnerReturnCode extends AbstractProtocol {

    public final static int ID = 901211;

    /**
     * 返回码(0为操作成功,其他都是错误码)
     */
    private int code;

    @Override
    public void decode(ByteBuf buffer) throws IOException {
        code = buffer.readInt();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(code);
    }

    @Override
    public int getId() {
        return ID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
