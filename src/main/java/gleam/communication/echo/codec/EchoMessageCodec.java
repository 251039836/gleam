package gleam.communication.echo.codec;

import java.util.List;

import gleam.communication.echo.protocol.EchoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class EchoMessageCodec extends ByteToMessageCodec<EchoMessage> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        EchoMessage request = new EchoMessage();
        request.decode(in);
        out.add(request);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EchoMessage msg, ByteBuf out) throws Exception {
        msg.encode(out);
    }

}
