package gleam.core;

/**
 * 实体提供类<br>
 * 创建对象时 注册实体相关组件
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface EntityProvider<T extends Entity<?>> {

    T get(long id);

}
