package gleam.util.tuple;

import java.util.Objects;

/**
 * 自定义KV数据类型
 *
 * @author ：cwx
 * @date ：Created in 2020
 * @version: $
 */
public class EntryData<T> {
    /**
     * @param id    KV表配置id
     * @param clazz 需要转换的数据类型(可自定义)
     */
    public static <T> EntryData<T> valueOf(int id, Class<T> clazz) {
        EntryData<T> data = new EntryData<>(id, clazz);
        return data;
    }

    /**
     * 离散表id 策划定义
     */
    protected final int id;

    /**
     * 数据类型 服务端添加
     */
    protected final Class<T> clazz;

    /**
     * 转换之后的值
     */
    protected T value;

    private EntryData(int id, Class<T> clazz) {
        this.id = id;
        this.clazz = clazz;
        this.value = null;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public int getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = Objects.requireNonNull(value);
    }

}
