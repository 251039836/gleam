package gleam.communication.server.impl;

import java.nio.ByteOrder;

import gleam.communication.ConnectionListener;
import gleam.communication.impl.CommonChannelHandler;
import gleam.communication.protocol.codec.ProtocolDecoder;
import gleam.communication.protocol.codec.ProtocolEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    /** 大端优先 */
    private final static ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    /** 消息包最大长度 */
    private final static int MAX_FRAME_LENGTH = 1 << 20;
    /** 长度偏移量 */
    private final static int LENGTH_FIELD_OFFSET = 0;
    /** 长度属性的长度 */
    private final static int LENGTH_FIELD_LENGTH = 4;
    /** 长度的补偿值 如果总长包括了头部(长度属性) 可以设为负数 */
    private final static int LENGTH_ADJUSTMENT = 0;
    /** 解包后需要跳过的字节数 */
    private final static int INITIAL_BYTES_TO_STRIP = 4;
    /** 超长包的处理方式true直接报错false解析完数据包再报错 */
    private final static boolean FAIL_FAST = true;
    /** 长度是否包含长度属性本身的长度 */
    private final static boolean LENGTH_INCLUDES_LENGTH_FIELD_LENGTH = false;

    private final LengthFieldPrepender lengthFieldEncoder = new LengthFieldPrepender(BYTE_ORDER, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT,
            LENGTH_INCLUDES_LENGTH_FIELD_LENGTH);

    private final ProtocolEncoder protocolEncoder = new ProtocolEncoder();

    private final CommonChannelHandler socketHandler;

    public SocketChannelInitializer(ConnectionListener connectionListener) {
        socketHandler = new CommonChannelHandler(connectionListener);
    }

    protected LengthFieldBasedFrameDecoder buildLengthFieldBasedFrameDecoder() {
        LengthFieldBasedFrameDecoder lengthDecoder = new LengthFieldBasedFrameDecoder(BYTE_ORDER, MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
                LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP, FAIL_FAST);
        return lengthDecoder;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 出
        pipeline.addLast("lengthFieldEncoder", lengthFieldEncoder);
        pipeline.addLast("protocolEncoder", protocolEncoder);
        // 进
        LengthFieldBasedFrameDecoder lengthFieldDecoder = buildLengthFieldBasedFrameDecoder();
        ProtocolDecoder protocolDecoder = new ProtocolDecoder();
        pipeline.addLast("lengthFieldDecoder", lengthFieldDecoder);
        pipeline.addLast("protocolDecoder", protocolDecoder);
        pipeline.addLast("socketHandler", socketHandler);
    }

}
