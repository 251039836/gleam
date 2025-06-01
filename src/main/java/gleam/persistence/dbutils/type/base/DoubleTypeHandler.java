package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class DoubleTypeHandler implements JdbcTypeHandler {

    @Override
    public Double getResult(ResultSet rs, int columnIndex) throws SQLException {
        double value = rs.getDouble(columnIndex);
        return value;
    }

}
