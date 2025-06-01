package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class FloatTypeHandler implements JdbcTypeHandler {

    @Override
    public Float getResult(ResultSet rs, int columnIndex) throws SQLException {
        float value = rs.getFloat(columnIndex);
        return value;
    }

}
