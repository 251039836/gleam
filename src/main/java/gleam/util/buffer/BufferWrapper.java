package gleam.util.buffer;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * buffer的封装类接口
 * 
 * @author hdh
 *
 */
public interface BufferWrapper {

    void clear();

    byte[] getData();

    boolean readBoolean();

    byte readByte();

    byte[] readBytes(int size);

    char readChar();

    <T extends ByteSerialize> T readData(Class<T> clazz);

    /**
     * 读取支持字节流序列化的数据列表<br>
     * 需有无参构造函数<br>
     * 长度单位为short
     * 
     * @param <T>
     * @param clazz
     * @param version
     * @return
     */
    <T extends ByteSerialize> Collection<T> readDatas(Class<T> clazz);

    /**
     * 读取数据列表
     * 
     * @param <T>
     * @param reader
     * @return
     */
    <T> Collection<T> readDatas(DataBufferReader<T> reader);

    double readDouble();

    int readerIndex();

    BufferWrapper readerIndex(int readerIndex);
    // write

    float readFloat();

    /**
     * 读取key为int,value为byte的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     *
     * @return
     */
    Map<Integer, Byte> readIBMap();

    /**
     * 读取key为int,value为int的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     * 
     * @return
     */
    Map<Integer, Integer> readIIMap();

    /**
     * 读取key为int,value为long的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     * 
     * @return
     */
    Map<Integer, Long> readILMap();

    /**
     * 读取key为byte,value为long的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     * 
     * @return
     */
    Map<Byte, Long> readBLMap();

    int readInt();

    /**
     * 读取int列表<br>
     * 长度单位为short
     * 
     * @return
     */
    Collection<Integer> readInts();

    /**
     * 读取key为long,value为int的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     * 
     * @return
     */
    Map<Long, Integer> readLIMap();

    /**
     * 读取key为long,value为long的map<br>
     * 长度字段为short<br>
     * 默认返回hashMap<br>
     * 最好自行使用其他容器
     * 
     * @return
     */
    Map<Long, Long> readLLMap();

    long readLong();

    /**
     * 读取long列表<br>
     * 长度单位为short
     * 
     * @return
     */
    Collection<Long> readLongs();

    short readShort();

    String readString();

    String readString(Charset charset);

    /**
     * 读取字符串列表<br>
     * 长度单位为short
     * 
     * @return
     */
    Collection<String> readStrings();

    BufferWrapper writeBoolean(boolean value);

    BufferWrapper writeByte(int value);

    // read

    BufferWrapper writeBytes(byte[] bytes);

    BufferWrapper writeChar(char value);

    <T extends ByteSerialize> BufferWrapper writeData(@Nonnull T value);

    /**
     * 写入支持字节流序列化数据的集合<br>
     * 长度单位为short
     * 
     * @param values
     * @return
     */
    <T extends ByteSerialize> BufferWrapper writeDatas(Collection<T> values);

    /**
     * 使用该buffer数据写入方法 写入该集合数据<br>
     * 长度单位为short
     * 
     * @param <T>
     * @param values
     * @param writer
     * @return
     */
    <T> BufferWrapper writeDatas(Collection<T> values, DataBufferWriter<T> writer);

    BufferWrapper writeDouble(double value);

    BufferWrapper writeFloat(float value);

    /**
     * 写入key为byte,value为byte的map<br>
     * 长度字段为short
     *
     * @param values
     * @return
     */
    BufferWrapper writeBLMap(Map<Byte, Long> values);

    /**
     * 写入key为int,value为byte的map<br>
     * 长度字段为short
     *
     * @param values
     * @return
     */
    BufferWrapper writeIBMap(Map<Integer, Byte> values);

    /**
     * 写入key和value都是Integer的map<br>
     * 长度字段为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeIIMap(Map<Integer, Integer> values);

    /**
     * 写入key为int,value为long的map<br>
     * 长度字段为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeILMap(Map<Integer, Long> values);

    BufferWrapper writeInt(int value);

    /**
     * 写入int集合<br>
     * 长度单位为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeInts(Collection<Integer> values);

    /**
     * 写入key为long,value为int的map<br>
     * 长度字段为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeLIMap(Map<Long, Integer> values);

    /**
     * 写入key和value都是Long的map<br>
     * 长度字段为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeLLMap(Map<Long, Long> values);

    BufferWrapper writeLong(long value);

    /**
     * 写入long集合<br>
     * 长度单位为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeLongs(Collection<Long> values);

    BufferWrapper writeLongs(long[] values);

    int writerIndex();

    BufferWrapper writerIndex(int writeIndex);

    BufferWrapper writeShort(int value);

    BufferWrapper writeString(String value);

    BufferWrapper writeString(String value, Charset charset);

    /**
     * 写入字符串集合<br>
     * 长度单位为short
     * 
     * @param values
     * @return
     */
    BufferWrapper writeStrings(Collection<String> values);
}
