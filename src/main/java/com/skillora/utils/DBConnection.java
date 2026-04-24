package com.skillora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    public static final String URL="jdbc:mysql://localhost:3306/esprit";
    public static final String USER="root";
    public static final String PASSWORD="";
    private Connection conn;
    private static DBConnection instance;

    private DBConnection(){
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection établie !!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static DBConnection getInstance(){
        if(instance == null){
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL,USER,PASSWORD);


    }
}
