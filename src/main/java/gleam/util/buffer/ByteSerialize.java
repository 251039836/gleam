package gleam.util.buffer;

/**
 * 字节序列化<br>
 * 若同1个类可能会被不同功能使用 或可能有版本区别<br>
 * 预先在读写时添加版本号<br>
 * 该版本号只适用于该接口实现类本身<br>
 * 
 */
public interface ByteSerialize {

    /**
     * 读取buffer还原对象
     * 
     * @param buffer
     */
    void readBuffer(BufferWrapper buffer);

    /**
     * 将对象写入buffer中
     * 
     * @param buffer
     */
    void writeBuffer(BufferWrapper buffer);

}
