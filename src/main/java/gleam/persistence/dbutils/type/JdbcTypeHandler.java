package gleam.persistence.dbutils.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

public interface JdbcTypeHandler {

    /**
     * 
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Nullable
    Object getResult(ResultSet rs, int columnIndex) throws SQLException;
}
