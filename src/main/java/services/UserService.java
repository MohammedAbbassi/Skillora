package services;

import entities.Role;
import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection connection;

    public UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────
    //  CRUD
    // ─────────────────────────────────────────────

    public void add(User user) throws SQLException {
        String req = "INSERT INTO `utilisateurs`(`nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            user.setIdUtilisateur((int) rs.getLong(1));
        }
        System.out.println("User added");
    }

    public void update(User user) throws SQLException {
        String req = "UPDATE utilisateurs SET nom_utilisateur = ?, email = ?, mot_de_passe = ?, prenom = ?, nom = ? WHERE id_utilisateur = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.setInt(6, user.getIdUtilisateur());
        pst.executeUpdate();
        System.out.println("User modified");
    }

    public void delete(User user) throws SQLException {
        String req = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, user.getIdUtilisateur());
        pst.executeUpdate();
        System.out.println("User deleted");
    }

    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM utilisateurs";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            users.add(mapRow(rs));
        }
        return users;
    }

    // ─────────────────────────────────────────────
    //  LOOKUPS
    // ─────────────────────────────────────────────

    public User getUserById(int id) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE id_utilisateur = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapRow(rs) : null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE email = ?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapRow(rs) : null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getUserByEmail(email) != null;
    }

    public User authenticate(String email, String password) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ? AND est_actif = 1";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setString(1, email);
        pst.setString(2, password);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapRow(rs) : null;
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUtilisateur((int) rs.getLong("id_utilisateur"));
        user.setNomUtilisateur(rs.getString("nom_utilisateur"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setPhotoProfil(rs.getString("photo_profil"));
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try { user.setRole(Role.valueOf(roleStr)); } catch (IllegalArgumentException ignored) {}
        }
        return user;
    }
}
