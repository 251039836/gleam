package gleam.util.buffer;

/**
 * buffer读取数据工具
 * 
 * @author hdh
 *
 * @param <T>
 */
@FunctionalInterface
public interface DataBufferReader<T> {

    /**
     * 读取buffer
     * 
     * @param buffer
     * @param data
     */
    T readData(BufferWrapper buffer);
}
