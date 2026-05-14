package com.skillora.shop.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ShopDatabaseMigrator {
    private static boolean migrated;

    private ShopDatabaseMigrator() {
    }

    public static synchronized void migrate(Connection conn) {
        if (migrated || conn == null) {
            return;
        }
        try {
            ensureProduit(conn);
            ensureEvaluation(conn);
            ensureCommande(conn);
            ensureCommandeProduit(conn);
            ensureCoupon(conn);
            ensureCouponUsage(conn);
            ensurePaiement(conn);
            migrated = true;
        } catch (SQLException e) {
            System.err.println("[ShopDB] Migration incomplete: " + e.getMessage());
        }
    }

    private static void ensureProduit(Connection conn) throws SQLException {
        if (!tableExists(conn, "produit")) {
            execute(conn,
                    "CREATE TABLE produit (" +
                            "id_produit INT NOT NULL AUTO_INCREMENT, " +
                            "nom VARCHAR(255) NOT NULL, " +
                            "description TEXT DEFAULT NULL, " +
                            "prix DECIMAL(10,2) NOT NULL, " +
                            "categorie ENUM('LIVRE','SERIE','FORMATION','KIT') NOT NULL DEFAULT 'FORMATION', " +
                            "langue VARCHAR(40) DEFAULT NULL, " +
                            "niveau ENUM('DEBUTANT','INTERMEDIAIRE','AVANCE') DEFAULT NULL, " +
                            "id_cours INT DEFAULT NULL, " +
                            "image VARCHAR(500) DEFAULT NULL, " +
                            "PRIMARY KEY (id_produit)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            return;
        }

        addColumnIfMissing(conn, "produit", "description", "TEXT DEFAULT NULL");
        addColumnIfMissing(conn, "produit", "prix", "DECIMAL(10,2) NOT NULL DEFAULT 0.00");
        addColumnIfMissing(conn, "produit", "categorie", "ENUM('LIVRE','SERIE','FORMATION','KIT') NOT NULL DEFAULT 'FORMATION'");
        addColumnIfMissing(conn, "produit", "langue", "VARCHAR(40) DEFAULT NULL");
        addColumnIfMissing(conn, "produit", "niveau", "ENUM('DEBUTANT','INTERMEDIAIRE','AVANCE') DEFAULT NULL");
        addColumnIfMissing(conn, "produit", "id_cours", "INT DEFAULT NULL");
        addColumnIfMissing(conn, "produit", "image", "VARCHAR(500) DEFAULT NULL");
    }

    private static void ensureEvaluation(Connection conn) throws SQLException {
        if (!tableExists(conn, "evaluation")) {
            execute(conn,
                    "CREATE TABLE evaluation (" +
                            "id_evaluation INT NOT NULL AUTO_INCREMENT, " +
                            "id_produit INT NOT NULL, " +
                            "id_utilisateur BIGINT NOT NULL, " +
                            "note INT NOT NULL, " +
                            "commentaire TEXT DEFAULT NULL, " +
                            "date_evaluation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (id_evaluation), " +
                            "UNIQUE KEY uk_eval_user_prod (id_produit, id_utilisateur)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            return;
        }

        addColumnIfMissing(conn, "evaluation", "id_evaluation", "INT NOT NULL AUTO_INCREMENT PRIMARY KEY");
        addColumnIfMissing(conn, "evaluation", "id_produit", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "evaluation", "id_utilisateur", "BIGINT NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "evaluation", "note", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "evaluation", "commentaire", "TEXT DEFAULT NULL");
        addColumnIfMissing(conn, "evaluation", "date_evaluation", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addIndexIfPossible(conn, "evaluation", "uk_eval_user_prod", "UNIQUE KEY uk_eval_user_prod (id_produit, id_utilisateur)");
    }

    private static void ensureCommande(Connection conn) throws SQLException {
        if (!tableExists(conn, "commande")) {
            execute(conn,
                    "CREATE TABLE commande (" +
                            "id_commande INT NOT NULL AUTO_INCREMENT, " +
                            "date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "total DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                            "statut ENUM('EN_ATTENTE','PAYEE','ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE', " +
                            "id_utilisateur BIGINT NOT NULL, " +
                            "PRIMARY KEY (id_commande)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            return;
        }

        addColumnIfMissing(conn, "commande", "date_commande", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing(conn, "commande", "total", "DECIMAL(10,2) NOT NULL DEFAULT 0.00");
        addColumnIfMissing(conn, "commande", "statut", "ENUM('EN_ATTENTE','PAYEE','ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE'");
        addColumnIfMissing(conn, "commande", "id_utilisateur", "BIGINT NOT NULL DEFAULT 0");
    }

    private static void ensureCommandeProduit(Connection conn) throws SQLException {
        if (!tableExists(conn, "commande_produit")) {
            execute(conn,
                    "CREATE TABLE commande_produit (" +
                            "id_commande INT NOT NULL, " +
                            "id_produit INT NOT NULL, " +
                            "quantite INT NOT NULL DEFAULT 1, " +
                            "prix_unitaire DECIMAL(10,2) NOT NULL, " +
                            "PRIMARY KEY (id_commande, id_produit)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            return;
        }

        addColumnIfMissing(conn, "commande_produit", "id_commande", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "commande_produit", "id_produit", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "commande_produit", "quantite", "INT NOT NULL DEFAULT 1");
        addColumnIfMissing(conn, "commande_produit", "prix_unitaire", "DECIMAL(10,2) NOT NULL DEFAULT 0.00");
    }

    private static void ensureCoupon(Connection conn) throws SQLException {
        if (!tableExists(conn, "coupon")) {
            execute(conn,
                    "CREATE TABLE coupon (" +
                            "id_coupon INT NOT NULL AUTO_INCREMENT, " +
                            "code VARCHAR(50) NOT NULL, " +
                            "type_reduction ENUM('POURCENTAGE','FIXE') NOT NULL, " +
                            "valeur_reduction DOUBLE NOT NULL, " +
                            "date_expiration DATETIME NOT NULL, " +
                            "max_utilisations INT NOT NULL DEFAULT 1, " +
                            "nombre_utilisations INT NOT NULL DEFAULT 0, " +
                            "montant_minimum DOUBLE NOT NULL DEFAULT 0, " +
                            "actif TINYINT(1) NOT NULL DEFAULT 1, " +
                            "date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (id_coupon), " +
                            "UNIQUE KEY uk_coupon_code (code)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static void ensureCouponUsage(Connection conn) throws SQLException {
        if (!tableExists(conn, "coupon_usage")) {
            execute(conn,
                    "CREATE TABLE coupon_usage (" +
                            "id_usage INT NOT NULL AUTO_INCREMENT, " +
                            "id_coupon INT NOT NULL, " +
                            "id_utilisateur BIGINT NOT NULL, " +
                            "id_commande INT NOT NULL, " +
                            "date_utilisation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (id_usage)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static void ensurePaiement(Connection conn) throws SQLException {
        if (!tableExists(conn, "paiement")) {
            execute(conn,
                    "CREATE TABLE paiement (" +
                            "id_paiement INT NOT NULL AUTO_INCREMENT, " +
                            "id_commande INT NOT NULL, " +
                            "payment_id VARCHAR(255) NOT NULL, " +
                            "montant DECIMAL(10,2) NOT NULL, " +
                            "statut VARCHAR(40) NOT NULL, " +
                            "date_paiement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (id_paiement)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void addColumnIfMissing(Connection conn, String tableName, String columnName, String definition) throws SQLException {
        if (!columnExists(conn, tableName, columnName)) {
            execute(conn, "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private static void addIndexIfPossible(Connection conn, String tableName, String indexName, String definition) {
        try {
            if (!indexExists(conn, tableName, indexName)) {
                execute(conn, "ALTER TABLE " + tableName + " ADD " + definition);
            }
        } catch (SQLException e) {
            System.err.println("[ShopDB] Cannot add index " + indexName + ": " + e.getMessage());
        }
    }

    private static boolean indexExists(Connection conn, String tableName, String indexName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getIndexInfo(conn.getCatalog(), null, tableName, false, false)) {
            while (rs.next()) {
                if (indexName.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }
}
