package gleam.persistence.dbutils.type.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import gleam.persistence.dbutils.type.JdbcTypeHandler;

public class ByteTypeHandler implements JdbcTypeHandler {

    @Override
    public Byte getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte value = rs.getByte(columnIndex);
        return value;
    }

}
