package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class ByteArrayTypeHandler implements JdbcTypeHandler {

    @Override
    public byte[] getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return bytes;
    }

}
