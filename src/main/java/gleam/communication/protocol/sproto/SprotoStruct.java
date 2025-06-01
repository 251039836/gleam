package gleam.communication.protocol.sproto;

import gleam.communication.protocol.AbstractProtocol;
import gleam.communication.protocol.sproto.decode.SprotoDecoder;
import gleam.communication.protocol.sproto.encode.SprotoEncoder;
import io.netty.buffer.ByteBuf;

/**
 * sproto编解码的结构体<br>
 * 被其他sproto协议引用<br>
 * 不可直接使用<br>
 * 不执行sproto的压缩
 * 
 * @author hdh
 *
 */
public abstract class SprotoStruct extends AbstractProtocol {

    @Override
    public void decode(ByteBuf buffer) throws Exception {
        SprotoDecoder decoder = new SprotoDecoder();
        decoder.readBuffer(buffer);
        decode(decoder);
    }

    protected abstract void decode(SprotoDecoder decoder) throws Exception;

    @Override
    public void encode(ByteBuf buffer) {
        SprotoEncoder encoder = new SprotoEncoder();
        encode(encoder);
        encoder.writeBuffer(buffer);
    }

    protected abstract void encode(SprotoEncoder encoder);

}
