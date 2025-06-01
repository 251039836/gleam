package gleam.communication.protocol.codec;

import gleam.communication.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 协议编码器<br>
 * 把协议转为byte数组写入缓存中
 * 
 * @author hdh
 *
 */
@Sharable
public class ProtocolEncoder extends MessageToByteEncoder<Protocol> {

    // private final PooledByteBufAllocator allocator = new
    // PooledByteBufAllocator(
    // PlatformDependent.directBufferPreferred());

    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol protocol, ByteBuf out) throws Exception {
//        BufferWrapper outBuffer = new DefaultBufferWrapper(out);
        int seq = protocol.getSeq();
        int id = protocol.getId();
        out.writeInt(seq);
        out.writeInt(id);
        protocol.encode(out);
    }

}
