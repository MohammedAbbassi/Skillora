package com.skillora.services;

import com.skillora.entities.Role;
import com.skillora.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import com.skillora.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private final Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    private static Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return Role.ETUDIANT;
        }
        try {
            return Role.valueOf(roleStr.trim());
        } catch (IllegalArgumentException e) {
            return Role.ETUDIANT;
        }
    }

    private static boolean looksLikeBcrypt(String s) {
        return s != null && s.startsWith("$2a$");
    }

    private static String hashIfNeeded(String plainOrHash) {
        if (plainOrHash == null) {
            return null;
        }
        if (looksLikeBcrypt(plainOrHash)) {
            return plainOrHash;
        }
        return BCrypt.hashpw(plainOrHash, BCrypt.gensalt());
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUtilisateur(rs.getLong("id_utilisateur"));
        user.setNomUtilisateur(rs.getString("nom_utilisateur"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setPrenom(rs.getString("prenom"));
        user.setNom(rs.getString("nom"));
        user.setPhotoProfil(rs.getString("photo_profil"));
        user.setRole(parseRole(rs.getString("role")));
        user.setEstActif(rs.getBoolean("est_actif"));
        user.setDateCreation(rs.getTimestamp("date_creation"));
        user.setDateModification(rs.getTimestamp("date_modification"));
        return user;
    }

    @Override
    public void add(User user) throws SQLException {
        String req = "INSERT INTO utilisateurs (nom_utilisateur, email, mot_de_passe, prenom, nom) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNomUtilisateur());
            pst.setString(2, user.getEmail());
            pst.setString(3, hashIfNeeded(user.getMotDePasse()));
            pst.setString(4, user.getPrenom());
            pst.setString(5, user.getNom());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setIdUtilisateur(rs.getLong(1));
                }
            }
        }
    }

    public User authenticate(String email, String plainPassword) throws SQLException {
        User u = getUserByEmail(email);
        if (u == null || !u.isEstActif()) {
            return null;
        }
        String stored = u.getMotDePasse();
        if (looksLikeBcrypt(stored)) {
            if (plainPassword != null && BCrypt.checkpw(plainPassword, stored)) {
                return u;
            }
            return null;
        }
        if (plainPassword != null && plainPassword.equals(stored)) {
            return u;
        }
        return null;
    }

    @Override
    public void update(User user) throws SQLException {
        String req = "UPDATE utilisateurs SET nom_utilisateur = ?, email = ?, mot_de_passe = ?, prenom = ?, nom = ? WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, user.getNomUtilisateur());
            pst.setString(2, user.getEmail());
            pst.setString(3, hashIfNeeded(user.getMotDePasse()));
            pst.setString(4, user.getPrenom());
            pst.setString(5, user.getNom());
            pst.setLong(6, user.getIdUtilisateur());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(User user) throws SQLException {
        try (PreparedStatement pst = cnx.prepareStatement("DELETE FROM utilisateurs WHERE id_utilisateur = ?")) {
            pst.setLong(1, user.getIdUtilisateur());
            pst.executeUpdate();
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM utilisateurs ORDER BY nom_utilisateur")) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    public User getUserByEmail(String email) throws SQLException {
        try (PreparedStatement pst = cnx.prepareStatement("SELECT * FROM utilisateurs WHERE email = ?")) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
}
