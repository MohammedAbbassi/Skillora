package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DBConnection {

    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = getSharedConnection();
            }
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/skillora",
                        "root",
                        ""
                );
            }
        } catch (SQLException e) {
            conn = null;
        }
        return conn;
    }

    private static Connection getSharedConnection() {
        try {
            Class<?> databaseClass = Class.forName("utils.MyDatabase");
            Method getInstance = databaseClass.getMethod("getInstance");
            Object database = getInstance.invoke(null);
            Method getCnx = databaseClass.getMethod("getCnx");
            Object connection = getCnx.invoke(database);
            return connection instanceof Connection ? (Connection) connection : null;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
