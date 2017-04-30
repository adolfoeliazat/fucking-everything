package org.sqlite;

import org.sqlite.jdbc4.JDBC4PreparedStatement;
import java.sql.SQLException;

public class IntoJDBC4PreparedStatement extends JDBC4PreparedStatement {
    public IntoJDBC4PreparedStatement(SQLiteConnection conn, String sql) throws SQLException {
        super(conn, sql);
    }

    public void setObject(int pos, Object value) throws SQLException {
        if (value instanceof java.util.Date) {
            setDateByMilliseconds(pos, ((java.util.Date) value).getTime());
        } else {
            super.setObject(pos, value);
        }
    }
}
