package gleam.persistence.dbutils.type;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcBeanTypeHandler<T> extends JdbcTypeHandler {

    Type getType();

    Object convertParam(T value);

    T getResult(ResultSet rs, int columnIndex) throws SQLException;
}
