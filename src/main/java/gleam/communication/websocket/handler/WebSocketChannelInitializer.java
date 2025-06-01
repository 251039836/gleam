package gleam.communication.websocket.handler;

import gleam.communication.ConnectionListener;
import gleam.communication.impl.CommonChannelHandler;
import gleam.communication.websocket.codec.WebSocketProtocolDecoder;
import gleam.communication.websocket.codec.WebSocketProtocolEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final WebSocketProtocolEncoder protocolEncoder = new WebSocketProtocolEncoder();

    private final WebSocketProtocolDecoder protocolDecoder = new WebSocketProtocolDecoder();
    private final CommonChannelHandler websocketHandler;

    public WebSocketChannelInitializer(ConnectionListener connectionListener) {
        websocketHandler = new CommonChannelHandler(connectionListener);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // TODO ssl
//        SSLContext sslContext = SslUtil.createSSLContext(keyType, sslFilePath, keyPassword);
//        // SSLEngine 此类允许使用ssl安全套接层协议进行安全通信
//        SSLEngine engine = sslContext.createSSLEngine();
//        engine.setUseClientMode(false);
//        pipeline.addLast("ssl",new SslHandler(engine));

        // 将请求与应答消息编码或者解码为HTTP消息
        pipeline.addLast("http-codec", new HttpServerCodec());
        // ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        // 将http消息的多个部分组合成一条完整的HTTP消息
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));

        ChannelHandler webSocketHandler = new WebSocketServerProtocolHandler("/");
        pipeline.addLast("webSocketHandler", webSocketHandler);

        // 出
        // pipeline.addLast("lengthFieldEncoder", lengthFieldEncoder);
        pipeline.addLast("protocolEncoder", protocolEncoder);
//        // 是否期望对荷载数据进行掩码-客户端发送的数据必须要掩码
//        boolean expectMaskedFrames = true;
//        // 是否允许WS扩展
//        boolean allowExtensions = true;
//        // Websocket最大荷载数据长度，超过该值抛出异常
//        int maxFramePayloadLength = 65535;
//        // 是否允许掩码缺失
//        boolean allowMaskMismatch = true;

//        WebSocket13FrameDecoder webSocketFrameDecoder = new WebSocket13FrameDecoder(expectMaskedFrames, allowExtensions, maxFramePayloadLength,
//                allowMaskMismatch);
//        pipeline.addLast(webSocketFrameDecoder);
        // 进
        // LengthFieldBasedFrameDecoder lengthFieldDecoder =
        // buildLengthFieldBasedFrameDecoder();
//        WebSocket13FrameDecoder protocolDecoder = new WebSocket13FrameDecoder();
//         pipeline.addLast("lengthFieldDecoder", lengthFieldDecoder);
        pipeline.addLast("protocolDecoder", protocolDecoder);
        pipeline.addLast("myWebsocketHandler", websocketHandler);
    }

//    protected LengthFieldBasedFrameDecoder buildLengthFieldBasedFrameDecoder() {
//        LengthFieldBasedFrameDecoder lengthDecoder = new LengthFieldBasedFrameDecoder(BYTE_ORDER, MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
//                LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP, FAIL_FAST);
//        return lengthDecoder;
//    }

}
