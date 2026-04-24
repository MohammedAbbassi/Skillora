package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/skillora",
                        "root",
                        ""
                );
                System.out.println("Connexion établie !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
        return conn;
    }
}