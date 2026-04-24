package services;

import entities.User;
import entities.Role;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(User user) throws SQLException {
        String req = "INSERT INTO `utilisateurs`(`nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.executeUpdate();
        
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            user.setIdUtilisateur(rs.getLong(1));
        }
        System.out.println("User added");
    }

    @Override
    public void update(User user) throws SQLException {
        String req = "UPDATE utilisateurs SET nom_utilisateur = ?, email = ?, mot_de_passe = ?, prenom = ?, nom = ? WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.setLong(6, user.getIdUtilisateur());
        pst.executeUpdate();
        System.out.println("User modified");
    }

    @Override
    public void delete(User user) throws SQLException {
        String req = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, user.getIdUtilisateur());
        pst.executeUpdate();
        System.out.println("User deleted");
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM utilisateurs";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            User user = new User();
            user.setIdUtilisateur(rs.getLong("id_utilisateur"));
            user.setNomUtilisateur(rs.getString("nom_utilisateur"));
            user.setEmail(rs.getString("email"));
            user.setMotDePasse(rs.getString("mot_de_passe"));
            user.setPrenom(rs.getString("prenom"));
            user.setNom(rs.getString("nom"));
            user.setPhotoProfil(rs.getString("photo_profil"));
            String roleStr = rs.getString("role");
            user.setRole(roleStr != null ? Role.valueOf(roleStr) : null);
            user.setEstActif(rs.getBoolean("est_actif"));
            user.setDateCreation(rs.getTimestamp("date_creation"));
            user.setDateModification(rs.getTimestamp("date_modification"));
            users.add(user);
        }
        return users;
    }

    public User getUserById(long id) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            User user = new User();
            user.setIdUtilisateur(rs.getLong("id_utilisateur"));
            user.setNomUtilisateur(rs.getString("nom_utilisateur"));
            user.setEmail(rs.getString("email"));
            user.setMotDePasse(rs.getString("mot_de_passe"));
            user.setPrenom(rs.getString("prenom"));
            user.setNom(rs.getString("nom"));
            user.setPhotoProfil(rs.getString("photo_profil"));
            String roleStr = rs.getString("role");
            user.setRole(roleStr != null ? Role.valueOf(roleStr) : null);
            user.setEstActif(rs.getBoolean("est_actif"));
            user.setDateCreation(rs.getTimestamp("date_creation"));
            user.setDateModification(rs.getTimestamp("date_modification"));
            return user;
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE email = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            User user = new User();
            user.setIdUtilisateur(rs.getLong("id_utilisateur"));
            user.setNomUtilisateur(rs.getString("nom_utilisateur"));
            user.setEmail(rs.getString("email"));
            user.setMotDePasse(rs.getString("mot_de_passe"));
            user.setPrenom(rs.getString("prenom"));
            user.setNom(rs.getString("nom"));
            user.setPhotoProfil(rs.getString("photo_profil"));
            String roleStr = rs.getString("role");
            user.setRole(roleStr != null ? Role.valueOf(roleStr) : null);
            user.setEstActif(rs.getBoolean("est_actif"));
            user.setDateCreation(rs.getTimestamp("date_creation"));
            user.setDateModification(rs.getTimestamp("date_modification"));
            return user;
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getUserByEmail(email) != null;
    }
}