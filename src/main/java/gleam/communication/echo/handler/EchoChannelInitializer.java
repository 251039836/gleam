package gleam.communication.echo.handler;

import gleam.communication.ConnectionListener;
import gleam.communication.echo.codec.EchoMessageCodec;
import gleam.communication.impl.CommonChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class EchoChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final CommonChannelHandler echoHandler;

    public EchoChannelInitializer(ConnectionListener connectionListener) {
        this.echoHandler = new CommonChannelHandler(connectionListener);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        EchoMessageCodec codec = new EchoMessageCodec();
        pipeline.addLast("echoCodec", codec);
        pipeline.addLast("echoHandler", echoHandler);
    }

}
