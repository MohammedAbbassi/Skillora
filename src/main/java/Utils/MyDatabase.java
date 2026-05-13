package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/skillora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String URL = readConfig("SKILLORA_DB_URL", DEFAULT_URL);
    private final String USER = readConfig("SKILLORA_DB_USER", "root");
    private final String PASSWORD = readConfig("SKILLORA_DB_PASSWORD", "");
    private Connection cnx;
    private static MyDatabase instance;

    private MyDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String readConfig(String key, String fallback) {
        String value = System.getProperty(key.toLowerCase().replace('_', '.'));
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
