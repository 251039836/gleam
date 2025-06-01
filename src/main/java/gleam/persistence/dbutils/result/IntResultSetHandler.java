package gleam.persistence.dbutils.result;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

public class IntResultSetHandler implements ResultSetHandler<Integer> {
    private static IntResultSetHandler instance = new IntResultSetHandler();

    public static IntResultSetHandler getInstance() {
        return instance;
    }

    @Override
    public Integer handle(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return 0;
        }
        int result = rs.getInt(1);
        return result;
    }

}
