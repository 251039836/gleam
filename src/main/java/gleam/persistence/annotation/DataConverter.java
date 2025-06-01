package gleam.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gleam.persistence.saver.converter.PersistenceDataConverter;

/**
 * 数据转化器<br>
 * {@link PersistenceDataConverter}
 * 
 * @author hdh
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DataConverter {

    @SuppressWarnings("rawtypes")
    Class<? extends PersistenceDataConverter> value();
}
