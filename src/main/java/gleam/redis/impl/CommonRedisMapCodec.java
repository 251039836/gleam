package gleam.redis.impl;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import gleam.redis.impl.codec.RedissonByteSerializeDecoder;
import gleam.redis.impl.codec.RedissonByteSerializeEncoder;
import gleam.redis.impl.codec.RedissonJsonDecoder;
import gleam.redis.impl.codec.RedissonJsonEncoder;
import gleam.util.buffer.ByteSerialize;

public class CommonRedisMapCodec<K, V> implements Codec {

    protected final Class<K> keyClazz;
    protected final Class<V> valueClazz;

    protected Encoder keyEncoder;
    protected Decoder<K> keyDecoder;

    protected Encoder valueEncoder;
    protected Decoder<V> valueDecoder;

    public CommonRedisMapCodec(Class<K> keyClazz, Class<V> valueClazz) {
        super();
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        init();
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return (Decoder<Object>) keyDecoder;
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return keyEncoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decoder<Object> getMapValueDecoder() {
        return (Decoder<Object>) valueDecoder;
    }

    @Override
    public Encoder getMapValueEncoder() {
        return valueEncoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decoder<Object> getValueDecoder() {
        return (Decoder<Object>) valueDecoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return valueEncoder;
    }

    protected void init() {
        keyEncoder = initEncoder(keyClazz);
        keyDecoder = initDecoder(keyClazz);
        valueEncoder = initEncoder(valueClazz);
        valueDecoder = initDecoder(valueClazz);
    }

    /**
     * 初始化解析器
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Decoder<T> initDecoder(Class<T> clazz) {
        // 基础数据类型
        if (clazz.equals(String.class)) {
            return (Decoder<T>) StringCodec.INSTANCE.getValueDecoder();
        } else if (clazz.equals(Integer.class)) {
            return (Decoder<T>) IntegerCodec.INSTANCE.getValueDecoder();
        } else if (clazz.equals(Long.class)) {
            return (Decoder<T>) LongCodec.INSTANCE.getValueDecoder();
        } else if (clazz.equals(Double.class)) {
            return (Decoder<T>) DoubleCodec.INSTANCE.getValueDecoder();
        }
        // 自定义序列化接口
        if (ByteSerialize.class.isAssignableFrom(clazz)) {
            RedissonByteSerializeDecoder decoder = new RedissonByteSerializeDecoder(clazz);
            return decoder;
        }
        // 默认使用json
        return new RedissonJsonDecoder<>(clazz);
    }

    /**
     * 初始化编译器
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    private <T> Encoder initEncoder(Class<T> clazz) {
        // 基础数据类型都是使用string
        if (clazz.equals(String.class)) {
            return StringCodec.INSTANCE.getValueEncoder();
        } else if (clazz.equals(Integer.class)) {
            return IntegerCodec.INSTANCE.getValueEncoder();
        } else if (clazz.equals(Long.class)) {
            return LongCodec.INSTANCE.getValueEncoder();
        } else if (clazz.equals(Double.class)) {
            return DoubleCodec.INSTANCE.getValueEncoder();
        }
        if (ByteSerialize.class.isAssignableFrom(clazz)) {
            return RedissonByteSerializeEncoder.getInstance();
        }
        // 默认使用json格式
        return RedissonJsonEncoder.getInstance();
    }

}
