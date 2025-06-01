package gleam.persistence.dbutils.result;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

public class LongResultSetHandler implements ResultSetHandler<Long> {
    private static LongResultSetHandler instance = new LongResultSetHandler();

    public static LongResultSetHandler getInstance() {
        return instance;
    }

    @Override
    public Long handle(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return 0l;
        }
        long result = rs.getLong(1);
        return result;
    }

}
