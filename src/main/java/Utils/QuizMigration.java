package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class QuizMigration {

    public static void migrate(Connection conn) {
        if (conn == null) return;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            Statement st = conn.createStatement();

            // Create quiz table if not exists
            ResultSet rs = meta.getTables(null, null, "quiz", null);
            if (!rs.next()) {
                st.executeUpdate(
                    "CREATE TABLE quiz (" +
                    "id_quiz INT NOT NULL AUTO_INCREMENT, " +
                    "titre VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "niveau VARCHAR(50) DEFAULT 'DEBUTANT', " +
                    "matiere VARCHAR(50) DEFAULT NULL, " +
                    "id_createur BIGINT(20) DEFAULT NULL, " +
                    "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (id_quiz)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );
                System.out.println("Auto-migrated: created quiz table.");
            }

            // Add quiz_id to question table if not exists
            rs = meta.getColumns(null, null, "question", "quiz_id");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE question ADD COLUMN quiz_id INT DEFAULT NULL AFTER id_utilisateur");
                st.executeUpdate("ALTER TABLE question ADD INDEX idx_question_quiz (quiz_id)");
                System.out.println("Auto-migrated: added quiz_id to question table.");
            }

            // Add enonce to question table if not exists
            rs = meta.getColumns(null, null, "question", "enonce");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE question ADD COLUMN enonce TEXT AFTER libelle");
                System.out.println("Auto-migrated: added enonce to question table.");
            }

            // Add type_question to question table if not exists
            rs = meta.getColumns(null, null, "question", "type_question");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE question ADD COLUMN type_question ENUM('QCU','QCM') DEFAULT 'QCU' AFTER enonce");
                System.out.println("Auto-migrated: added type_question to question table.");
            }

            // Add image_path to question table if not exists
            rs = meta.getColumns(null, null, "question", "image_path");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE question ADD COLUMN image_path VARCHAR(255) DEFAULT NULL AFTER type_question");
                System.out.println("Auto-migrated: added image_path to question table.");
            }

        } catch (Exception e) {
            System.err.println("Quiz migration failed: " + e.getMessage());
        }
    }
}
