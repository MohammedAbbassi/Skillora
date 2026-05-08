import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateDatabase {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/skillora?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Adding xp_points and streak_days columns to utilisateurs...");
            
            try {
                stmt.executeUpdate("ALTER TABLE utilisateurs ADD COLUMN xp_points INT DEFAULT 0");
                System.out.println("Added xp_points column.");
            } catch (Exception e) {
                System.out.println("xp_points column might already exist: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE utilisateurs ADD COLUMN streak_days INT DEFAULT 0");
                System.out.println("Added streak_days column.");
            } catch (Exception e) {
                System.out.println("streak_days column might already exist: " + e.getMessage());
            }
            
            System.out.println("Database updated successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
