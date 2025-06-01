package gleam.redis.impl.codec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.redisson.client.protocol.Encoder;

import gleam.util.json.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class RedissonJsonEncoder implements Encoder {
    private static RedissonJsonEncoder instance = new RedissonJsonEncoder();

    public static RedissonJsonEncoder getInstance() {
        return instance;
    }

    @Override
    public ByteBuf encode(Object in) throws IOException {
        String json = JsonUtil.toJson(in);
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeCharSequence(json, StandardCharsets.UTF_8);
        return out;
    }

}
