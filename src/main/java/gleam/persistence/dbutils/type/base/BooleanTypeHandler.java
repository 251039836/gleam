package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class BooleanTypeHandler implements JdbcTypeHandler {

    @Override
    public Boolean getResult(ResultSet rs, int columnIndex) throws SQLException {
        boolean value = rs.getBoolean(columnIndex);
        return value;
    }

}
