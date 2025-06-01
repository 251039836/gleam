package gleam.util.buffer;

import javax.annotation.Nonnull;

/**
 * 数据写入buffer的工具
 * 
 * @author hdh
 *
 * @param <T>
 */
@FunctionalInterface
public interface DataBufferWriter<T> {

    /**
     * 写入数据到buffer中
     * 
     * @param data
     * @param buffer
     */
    void writeData(@Nonnull T data, BufferWrapper buffer);
}
