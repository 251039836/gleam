package gleam.redis.impl.codec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import gleam.util.json.JsonUtil;
import io.netty.buffer.ByteBuf;

public class RedissonJsonDecoder<T> implements Decoder<T> {

    private final Class<T> clazz;

    public RedissonJsonDecoder(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public T decode(ByteBuf buf, State state) throws IOException {
        String json = buf.toString(StandardCharsets.UTF_8);
        buf.readerIndex(buf.readableBytes());
        T bean = JsonUtil.toObject(json, clazz);
        return bean;
    }

    public Class<T> getClazz() {
        return clazz;
    }

}
