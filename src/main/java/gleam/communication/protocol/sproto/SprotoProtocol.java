package gleam.communication.protocol.sproto;

import gleam.communication.protocol.AbstractProtocol;
import gleam.communication.protocol.sproto.decode.SprotoDecoder;
import gleam.communication.protocol.sproto.encode.SprotoEncoder;
import gleam.communication.protocol.sproto.util.SprotoPack;
import io.netty.buffer.ByteBuf;

/**
 * sproto编解码的协议
 * 
 * @author hdh
 *
 */
public abstract class SprotoProtocol extends AbstractProtocol {

    @Override
    public void decode(ByteBuf buffer) throws Exception {
        ByteBuf unpackBuffer = SprotoPack.unpack(buffer);
        SprotoDecoder decoder = new SprotoDecoder();
        decoder.readBuffer(unpackBuffer);
        decode(decoder);
    }

    protected abstract void decode(SprotoDecoder decoder) throws Exception;

    @Override
    public void encode(ByteBuf buffer) {
        int writerIndex = buffer.writerIndex();
        SprotoEncoder encoder = new SprotoEncoder();
        encode(encoder);
        encoder.writeBuffer(buffer);
        SprotoPack.pack(buffer, writerIndex);
    }

    protected abstract void encode(SprotoEncoder encoder);

}
