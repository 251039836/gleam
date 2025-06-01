package gleam.persistence.dbutils.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractJdbcBeanTypeHandler<T> implements JdbcBeanTypeHandler<T> {

    private final Type type;

    public AbstractJdbcBeanTypeHandler() {
        this.type = getSuperclassTypeParameter(getClass());
    }

    public AbstractJdbcBeanTypeHandler(Type type) {
        this.type = type;
    }

    protected Type getSuperclassTypeParameter(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof Class) {
            // try to climb up the hierarchy until meet something useful
            if (JdbcBeanTypeHandler.class != genericSuperclass) {
                return getSuperclassTypeParameter(clazz.getSuperclass());
            }
            throw new IllegalArgumentException("'" + getClass() + "' extends JdbcTypeHandler but misses the type parameter. ");
        }

        Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        return rawType;
    }

    @Override
    public Type getType() {
        return type;
    }

}
