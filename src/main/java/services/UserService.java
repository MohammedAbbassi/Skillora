package services;

import entities.Role;
import entities.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private final Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(User user) throws SQLException {
        String req = "INSERT INTO utilisateurs(nom_utilisateur, email, mot_de_passe, prenom, nom) VALUES (?, ?, ?, ?, ?)";
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
    }

    @Override
    public void update(User user) throws SQLException {
        String req = "UPDATE utilisateurs SET nom_utilisateur=?, email=?, mot_de_passe=?, prenom=?, nom=? WHERE id_utilisateur=?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.setLong(6, user.getIdUtilisateur());
        pst.executeUpdate();
    }

    @Override
    public void delete(User user) throws SQLException {
        String req = "DELETE FROM utilisateurs WHERE id_utilisateur=?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, user.getIdUtilisateur());
        pst.executeUpdate();
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM utilisateurs";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            users.add(mapUser(rs));
        }
        return users;
    }

    public User getUserById(long id) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE id_utilisateur=?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapUser(rs) : null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE email=?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapUser(rs) : null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getUserByEmail(email) != null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
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
}
