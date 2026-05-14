package com.skillora.shop.utils;

import com.skillora.shop.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String url;
    private final String user;
    private final String password;
    private Connection cnx;
    private static MyDatabase instance;

    private MyDatabase() {
        this.url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/skillora_shop?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.user = AppConfig.get("db.user", "root");
        this.password = AppConfig.get("db.password", "");
        connect();
    }

    private void connect() {
        cnx = getDashboardConnection();
        if (cnx != null) {
            return;
        }

        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.err.println("[ShopDB] Cannot connect to " + url + ": " + e.getMessage());
            cnx = null;
        }
    }

    public static synchronized MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return cnx;
    }

    private Connection getDashboardConnection() {
        try {
            Connection shared = utils.MyDatabase.getInstance().getCnx();
            if (shared != null && !shared.isClosed()) {
                return shared;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
