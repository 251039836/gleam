package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class StringTypeHandler implements JdbcTypeHandler {

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value;
    }

}
