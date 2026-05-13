package services;

import entities.User;
import interfaces.IService;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    Connection cnx;
    private static final String DEFAULT_ADMIN_EMAIL = "admin";

    public ServiceUser() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("ServiceUser: Connection to database failed!");
        } else {
            autoMigrate();
        }
    }

    private void checkConnection() throws SQLException {
        if (cnx == null) {
            throw new SQLException("Database connection is not available. Please check if your MySQL server is running.");
        }
    }
    
    private void autoMigrate() {
        try {
            if (cnx != null) {
                addColumnIfMissing("est_en_ligne", "ALTER TABLE utilisateurs ADD COLUMN est_en_ligne tinyint(1) NOT NULL DEFAULT 0;");
                addColumnIfMissing("ranked_points", "ALTER TABLE utilisateurs ADD COLUMN ranked_points int(11) NOT NULL DEFAULT 0 AFTER xp_points;");
                addColumnIfMissing("certificates_count", "ALTER TABLE utilisateurs ADD COLUMN certificates_count int(11) NOT NULL DEFAULT 0 AFTER streak_days;");
                addColumnIfMissing("reset_token", "ALTER TABLE utilisateurs ADD COLUMN reset_token VARCHAR(255) DEFAULT NULL AFTER certificates_count;");
                addColumnIfMissing("reset_token_expiry", "ALTER TABLE utilisateurs ADD COLUMN reset_token_expiry TIMESTAMP NULL DEFAULT NULL AFTER reset_token;");
            }
        } catch (SQLException e) {
            System.err.println("Auto-migration failed: " + e.getMessage());
        }
    }

    private void addColumnIfMissing(String columnName, String sql) throws SQLException {
        DatabaseMetaData meta = cnx.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, "utilisateurs", columnName)) {
            if (!rs.next()) {
                try (Statement st = cnx.createStatement()) {
                    st.executeUpdate(sql);
                }
                System.out.println("Auto-migrated database: added " + columnName + " to utilisateurs table.");
            }
        }
    }

    @Override
    public void add(User user) throws SQLException {
        checkConnection();
        String hashedPassword = BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt());
<<<<<<< HEAD
        String req = "INSERT INTO utilisateurs(nom_utilisateur, email, mot_de_passe, prenom, nom, role, pays, est_actif, xp_points, ranked_points, streak_days, certificates_count, photo_profil) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
=======
        String req = "INSERT INTO utilisateurs(nom_utilisateur, email, mot_de_passe, prenom, nom, role, pays, est_actif, xp_points, ranked_points, streak_days, certificates_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
>>>>>>> mouayed
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, user.getNomUtilisateur());
            pst.setString(2, user.getEmail());
            pst.setString(3, hashedPassword);
            pst.setString(4, user.getPrenom() != null ? user.getPrenom() : "");
            pst.setString(5, user.getNom() != null ? user.getNom() : "");
            pst.setString(6, user.getRole() != null ? user.getRole() : "ETUDIANT");
            pst.setString(7, user.getPays() != null ? user.getPays() : "Tunisia");
            pst.setBoolean(8, true);
            pst.setInt(9, user.getXpPoints());
            pst.setInt(10, user.getRankedPoints());
            pst.setInt(11, user.getStreakDays());
            pst.setInt(12, user.getCertificatesCount());
<<<<<<< HEAD
            pst.setString(13, user.getPhotoProfil());
=======
>>>>>>> mouayed
            pst.executeUpdate();
        }
        System.out.println("User added to database with BCrypt");
    }

    @Override
    public void update(User user) throws SQLException {
        checkConnection();
<<<<<<< HEAD
        String req = "UPDATE utilisateurs SET nom_utilisateur=?, email=?, prenom=?, nom=?, role=?, pays=?, est_actif=?, xp_points=?, ranked_points=?, streak_days=?, certificates_count=?, photo_profil=? WHERE id_utilisateur=?";
=======
        String req = "UPDATE utilisateurs SET nom_utilisateur=?, email=?, prenom=?, nom=?, role=?, pays=?, est_actif=?, xp_points=?, ranked_points=?, streak_days=?, certificates_count=? WHERE id_utilisateur=?";
>>>>>>> mouayed
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, user.getNomUtilisateur());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPrenom());
            pst.setString(4, user.getNom());
            pst.setString(5, user.getRole());
            pst.setString(6, user.getPays());
            pst.setBoolean(7, user.isEstActif());
            pst.setInt(8, user.getXpPoints());
            pst.setInt(9, user.getRankedPoints());
            pst.setInt(10, user.getStreakDays());
            pst.setInt(11, user.getCertificatesCount());
<<<<<<< HEAD
            pst.setString(12, user.getPhotoProfil());
            pst.setInt(13, user.getId());
=======
            pst.setInt(12, user.getId());
>>>>>>> mouayed
            pst.executeUpdate();
        }
        System.out.println("User modifie");
    }

<<<<<<< HEAD
    public void updateProfilePhotoByEmail(String email, String base64Photo) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET photo_profil = ? WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, base64Photo);
            pst.setString(2, email);
            pst.executeUpdate();
        }
    }

=======
>>>>>>> mouayed
    public void updateRole(int userId, String newRole) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET role=? WHERE id_utilisateur=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, newRole);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    public void toggleStatus(int userId, boolean isActive) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET est_actif=? WHERE id_utilisateur=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setBoolean(1, isActive);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(User user) throws SQLException {
        checkConnection();
        String req = "DELETE FROM utilisateurs WHERE id_utilisateur=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        }
        System.out.println("User supprime");
    }

    @Override
    public List<User> getAll() throws SQLException {
        checkConnection();
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM utilisateurs";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    public User login(String email, String motDePasse) throws SQLException {
        checkConnection();
        String req = "SELECT * FROM utilisateurs WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
            String storedPassword = rs.getString("mot_de_passe");
            boolean authenticated = false;

            // Check if it's a valid BCrypt hash
            if (storedPassword != null && storedPassword.startsWith("$2a$")) {
                try {
                    if (BCrypt.checkpw(motDePasse, storedPassword)) {
                        authenticated = true;
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid BCrypt hash in database for email: " + email);
                }
            } else {
                // Fallback for plain text (migration period)
                if (motDePasse.equals(storedPassword)) {
                    authenticated = true;
                    // Auto-migrate to BCrypt
                    String newHash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
                    String updateReq = "UPDATE utilisateurs SET mot_de_passe = ? WHERE email = ?";
                    try (PreparedStatement updatePst = cnx.prepareStatement(updateReq)) {
                        updatePst.setString(1, newHash);
                        updatePst.setString(2, email);
                        updatePst.executeUpdate();
                    }
                    System.out.println("Auto-migrated user " + email + " to BCrypt");
                }
            }

            if (authenticated) {
                User u = mapUser(rs);
                u.setEstEnLigne(true);
                
                String updateOnlineReq = "UPDATE utilisateurs SET est_en_ligne = true WHERE id_utilisateur = ?";
                try (PreparedStatement updateOnlinePst = cnx.prepareStatement(updateOnlineReq)) {
                    updateOnlinePst.setInt(1, u.getId());
                    updateOnlinePst.executeUpdate();
                }
                
                return u;
            }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        checkConnection();
        String req = "SELECT 1 FROM utilisateurs WHERE email = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean usernameExists(String nomUtilisateur) throws SQLException {
        checkConnection();
        String req = "SELECT 1 FROM utilisateurs WHERE nom_utilisateur = ? LIMIT 1";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, nomUtilisateur);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        checkConnection();
        String req = "SELECT * FROM utilisateurs WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

<<<<<<< HEAD
=======
    public void updateProfilePhotoByEmail(String email, String base64Photo) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET photo_profil = ? WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, base64Photo);
            pst.setString(2, email);
            pst.executeUpdate();
        }
    }

>>>>>>> mouayed
    public void createAdminIfNotExists() throws SQLException {
        checkConnection();
        String checkReq = "SELECT COUNT(*) FROM utilisateurs WHERE role = 'ADMIN'";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(checkReq)) {
            if (rs.next() && rs.getInt(1) == 0) {
                User admin = new User();
                admin.setNomUtilisateur("admin");
                admin.setEmail(DEFAULT_ADMIN_EMAIL);
                admin.setMotDePasse(defaultAdminPassword());
                admin.setPrenom("System");
                admin.setNom("Admin");
                admin.setRole("ADMIN");
                add(admin);
                System.out.println("Default admin account created: " + DEFAULT_ADMIN_EMAIL);
            }
        }
    }

    private String defaultAdminPassword() {
        String configured = System.getProperty("skillora.admin.password");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SKILLORA_ADMIN_PASSWORD");
        }
        return configured == null || configured.isBlank() ? "admin" : configured;
    }

    public User getById(int id) throws SQLException {
        checkConnection();
        String req = "SELECT * FROM utilisateurs WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public void setOffline(int userId) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET est_en_ligne = false WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }

    public void setOnline(int userId) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET est_en_ligne = true WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }

    public void setResetToken(String email, String token) throws SQLException {
        checkConnection();
        String req = "UPDATE utilisateurs SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, token);
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 3600000)); // 1 hour expiry
            pst.setString(3, email);
            pst.executeUpdate();
        }
    }

    public User getUserByResetToken(String token) throws SQLException {
        checkConnection();
        String req = "SELECT * FROM utilisateurs WHERE reset_token = ? AND reset_token_expiry > ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, token);
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        checkConnection();
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String req = "UPDATE utilisateurs SET mot_de_passe = ?, reset_token = NULL, reset_token_expiry = NULL WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, hashedPassword);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id_utilisateur"));
        u.setNomUtilisateur(rs.getString("nom_utilisateur"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setPrenom(rs.getString("prenom"));
        u.setNom(rs.getString("nom"));
        u.setPhotoProfil(rs.getString("photo_profil"));
        u.setRole(rs.getString("role"));
        u.setPays(rs.getString("pays"));
        u.setEstActif(rs.getBoolean("est_actif"));
        u.setXpPoints(rs.getInt("xp_points"));
        u.setRankedPoints(rs.getInt("ranked_points"));
        u.setStreakDays(rs.getInt("streak_days"));
        u.setCertificatesCount(rs.getInt("certificates_count"));
        u.setEstEnLigne(rs.getBoolean("est_en_ligne"));
        u.setResetToken(rs.getString("reset_token"));
        u.setResetTokenExpiry(rs.getTimestamp("reset_token_expiry"));
        u.setDateCreation(rs.getTimestamp("date_creation"));
        u.setDateModification(rs.getTimestamp("date_modification"));
        return u;
    }
}
