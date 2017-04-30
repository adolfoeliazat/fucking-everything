package org.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class IntoSQLiteConnection extends SQLiteConnection {
    public IntoSQLiteConnection(String url, String fileName) throws SQLException {
        super(url, fileName);
    }

    public IntoSQLiteConnection(String url, String fileName, Properties prop) throws SQLException {
        super(url, fileName, prop);
    }

    public PreparedStatement prepareStatement(String sql, int rst, int rsc, int rsh) throws SQLException {
        checkOpen();
        checkCursor(rst, rsc, rsh);

        return new IntoJDBC4PreparedStatement((SQLiteConnection)this, sql);
    }
}
