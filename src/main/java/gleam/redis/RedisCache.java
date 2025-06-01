package gleam.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis 缓存注解 标注在类上<br>
 * 用于redis中映射Rmap的key<br>
 * 类似mysql的tablename
 * 
 * @author Jeremy
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {

    /**
     * hash name
     * 
     * @return
     */
    String value();
}
