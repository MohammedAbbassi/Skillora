package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private  final  String URL="jdbc:mysql://localhost:3306/skillora";
    private  final  String USER="root";
    private  final  String PASSWORD="";
    private Connection cnx;
    private  static  MyDatabase instance ;

    private String lastError;

    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            this.lastError = e.getMessage();
            System.err.println("Database connection failed: " + lastError);
        }
    }

    public void reconnect() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
            }
        } catch (SQLException ignored) {}
        
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            lastError = null;
            System.out.println("Reconnected to database");
        } catch (SQLException e) {
            this.lastError = e.getMessage();
            System.err.println("Database reconnection failed: " + lastError);
        }
    }

    public static MyDatabase getInstance() {

        if (instance == null)
            instance = new MyDatabase();
        return instance;
    }

    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }

    public Connection getCnx() {
        return cnx;
    }
}

