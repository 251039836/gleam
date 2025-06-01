package gleam.communication.protocol;

import java.io.IOException;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import io.netty.buffer.ByteBuf;

/**
 * protobuf编解码的协议
 * 
 * @author hdh
 *
 * @param <T> 该协议在protobuf中对应的协议
 */
public abstract class ProtobufProtocol<T extends MessageLite> extends AbstractProtocol {

    /**
     * 根据该协议生成protobuf协议
     * 
     * @return
     */
    protected abstract T buildProto();

    /**
     * 解码
     * 
     * @param buffer
     * @throws IOException
     */
    @Override
    public void decode(ByteBuf buffer) throws IOException {
        int length = buffer.readableBytes();
        byte[] protoBytes = new byte[length];
        buffer.readBytes(protoBytes);
        Parser<T> parser = getParser();
        T proto = parser.parseFrom(protoBytes);
        parseProto(proto);
    }

    /**
     * 编码
     * 
     * @param buffer
     */
    @Override
    public void encode(ByteBuf buffer) {
        T proto = buildProto();
        if (proto != null) {
            byte[] protoBytes = proto.toByteArray();
            buffer.writeBytes(protoBytes);
        }
    }

    /**
     * 获取该协议对应的protobuf协议解析器
     * 
     * @return
     */
    protected abstract Parser<T> getParser();

    /**
     * 解析protobuf协议
     * 
     * @param proto
     */
    protected abstract void parseProto(T proto);
}
