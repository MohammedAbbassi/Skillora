package services;

import entities.Role;
import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private Connection connection;

    public UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String req = "INSERT INTO utilisateurs (nom_utilisateur, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getNomUtilisateur());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getMotDePasse());
        ps.setString(4, user.getRole().name());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            user.setIdUtilisateur(rs.getInt(1));
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String req = "UPDATE utilisateurs SET nom_utilisateur=?, email=?, mot_de_passe=?, role=?, photo_profil=? WHERE id_utilisateur=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, user.getNomUtilisateur());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getMotDePasse());
        ps.setString(4, user.getRole().name());
        ps.setString(5, user.getPhotoProfil());
        ps.setInt(6, user.getIdUtilisateur());
        ps.executeUpdate();
    }

    @Override
    public void delete(User user) throws SQLException {
        String req = "DELETE FROM utilisateurs WHERE id_utilisateur=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, user.getIdUtilisateur());
        ps.executeUpdate();
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM utilisateurs";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            User u = new User();
            u.setIdUtilisateur(rs.getInt("id_utilisateur"));
            u.setNomUtilisateur(rs.getString("nom_utilisateur"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setPhotoProfil(rs.getString("photo_profil"));
            list.add(u);
        }
        return list;
    }

    public User getById(int id) throws SQLException {
        String req = "SELECT * FROM utilisateurs WHERE id_utilisateur=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            User u = new User();
            u.setIdUtilisateur(rs.getInt("id_utilisateur"));
            u.setNomUtilisateur(rs.getString("nom_utilisateur"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setPhotoProfil(rs.getString("photo_profil"));
            return u;
        }
        return null;
    }
}
