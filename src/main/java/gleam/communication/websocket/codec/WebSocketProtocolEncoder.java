package gleam.communication.websocket.codec;

import java.util.List;

import gleam.communication.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

@Sharable
public class WebSocketProtocolEncoder extends MessageToMessageEncoder<Protocol> {

    // private final PooledByteBufAllocator allocator = new
    // PooledByteBufAllocator(
    // PlatformDependent.directBufferPreferred());

//    @Override
//    protected void encode(ChannelHandlerContext ctx, Protocol protocol, ByteBuf out) throws Exception {
//        out.writeByte(130);
//        int beginIndex = out.writerIndex();
//        int seq = protocol.getSeq();
//        int id = protocol.getId();
//        out.writeInt(0);
//        out.writeInt(seq);
//        out.writeInt(id);
//        protocol.encode(out);
//        int endIndex = out.writerIndex();
//        int length = endIndex - beginIndex - 4;
//        out.writerIndex(beginIndex);
//        out.writeInt(length);
//        out.writerIndex(endIndex);
//    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol msg, List<Object> out) throws Exception {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
        ByteBuf buffer = frame.content();
        int seq = msg.getSeq();
        int id = msg.getId();
        buffer.writeInt(seq);// 序号
        buffer.writeInt(id);// 协议id
        msg.encode(buffer);// 协议内容
        out.add(frame);
    }

}
