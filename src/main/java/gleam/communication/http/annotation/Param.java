package gleam.communication.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法形参注解, 此值对应http请求参数, 若不加此参数,默认是形参名
 * 
 * @author Jeremy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Param {

    String value() default "";

}
