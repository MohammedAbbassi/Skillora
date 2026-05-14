package utils;

import java.sql.Connection;

public class MyBD {
    private Connection conn;

    private static MyBD instance;

    private MyBD() {
        conn = MyDatabase.getInstance().getCnx();
    }

    public static MyBD getInstance() {
        if (instance == null) {
            return instance = new MyBD();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
