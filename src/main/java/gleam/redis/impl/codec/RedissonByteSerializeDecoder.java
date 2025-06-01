package gleam.redis.impl.codec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import gleam.util.buffer.BufferWrapper;
import gleam.util.buffer.ByteSerialize;
import gleam.util.buffer.DefaultBufferWrapper;
import io.netty.buffer.ByteBuf;

public class RedissonByteSerializeDecoder<T extends ByteSerialize> implements Decoder<T> {

    private final Class<T> clazz;

    public RedissonByteSerializeDecoder(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public T decode(ByteBuf buf, State state) throws IOException {
        T bean = null;
        try {
            bean = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IOException("clazz[" + clazz.getName() + "] newInstance error.", e);
        }
        BufferWrapper buffer = new DefaultBufferWrapper(buf);
        bean.readBuffer(buffer);
        return bean;
    }

}
