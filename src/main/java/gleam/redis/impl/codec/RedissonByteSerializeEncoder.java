package gleam.redis.impl.codec;

import java.io.IOException;

import org.redisson.client.protocol.Encoder;

import gleam.util.buffer.ByteSerialize;
import gleam.util.buffer.DefaultBufferWrapper;
import io.netty.buffer.ByteBuf;

public class RedissonByteSerializeEncoder implements Encoder {
    private static RedissonByteSerializeEncoder instance = new RedissonByteSerializeEncoder();

    public static RedissonByteSerializeEncoder getInstance() {
        return instance;
    }

    @Override
    public ByteBuf encode(Object in) throws IOException {
        ByteSerialize bean = (ByteSerialize) in;
        DefaultBufferWrapper buffer = new DefaultBufferWrapper();
        bean.writeBuffer(buffer);
        return buffer.getBuffer();
    }

}
