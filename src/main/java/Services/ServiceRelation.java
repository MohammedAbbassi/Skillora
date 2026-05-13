package services;

import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRelation {
    private Connection cnx;

    public ServiceRelation() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("ServiceRelation: Connection to database failed!");
        } else {
            autoMigrate();
        }
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Database connection is not available.");
        }
    }

    private void autoMigrate() {
        try {
            DatabaseMetaData meta = cnx.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "relations", null)) {
                if (!rs.next()) {
                    try (Statement st = cnx.createStatement()) {
                        st.executeUpdate(
                            "CREATE TABLE `relations` (" +
                            "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                            "  `utilisateur_id` bigint(20) NOT NULL," +
                            "  `utilisateur_cible_id` bigint(20) NOT NULL," +
                            "  `type` enum('FRIEND','BLOCKED') NOT NULL DEFAULT 'FRIEND'," +
                            "  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()," +
                            "  PRIMARY KEY (`id`)," +
                            "  UNIQUE KEY `uq_relation` (`utilisateur_id`,`utilisateur_cible_id`)," +
                            "  KEY `idx_relation_utilisateur` (`utilisateur_id`)," +
                            "  KEY `idx_relation_cible` (`utilisateur_cible_id`)," +
                            "  CONSTRAINT `fk_relation_utilisateur` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE," +
                            "  CONSTRAINT `fk_relation_cible` FOREIGN KEY (`utilisateur_cible_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
                        );
                        System.out.println("Auto-migrated database: created relations table.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Auto-migration failed: " + e.getMessage());
        }
    }

    public String getRelationStatus(int userId, int targetId) {
        try {
            checkConnection();
            String sql = "SELECT type FROM relations WHERE utilisateur_id = ? AND utilisateur_cible_id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, targetId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("type");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addFriend(int userId, int friendId) throws SQLException {
        checkConnection();
        String sql = "INSERT INTO relations (utilisateur_id, utilisateur_cible_id, type) VALUES (?, ?, 'FRIEND') " +
                     "ON DUPLICATE KEY UPDATE type = 'FRIEND'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.executeUpdate();
        }
    }

    public void blockUser(int userId, int blockId) throws SQLException {
        checkConnection();
        String sql = "INSERT INTO relations (utilisateur_id, utilisateur_cible_id, type) VALUES (?, ?, 'BLOCKED') " +
                     "ON DUPLICATE KEY UPDATE type = 'BLOCKED'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, blockId);
            ps.executeUpdate();
        }
    }

    public void removeRelation(int userId, int targetId) throws SQLException {
        checkConnection();
        String sql = "DELETE FROM relations WHERE utilisateur_id = ? AND utilisateur_cible_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, targetId);
            ps.executeUpdate();
        }
    }

    public List<Integer> getFriendsIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        try {
            checkConnection();
            String sql = "SELECT utilisateur_cible_id FROM relations WHERE utilisateur_id = ? AND type = 'FRIEND'";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt("utilisateur_cible_id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
