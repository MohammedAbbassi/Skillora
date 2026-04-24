package Services;

import Entites.User;
import Interffaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCRUD implements InterfaceCRUD<User> {
    Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "insert into utilisateurs (nom_utilisateur, email, mot_de_passe, prenom, nom) " +
                "values('" + user.getNomUtilisateur() + "','"
                + user.getEmail() + "','"
                + user.getMotDePasse() + "','"
                + user.getPrenom() + "','"
                + user.getNom() + "')";
        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("User ajoute !!");
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "update utilisateurs set nom_utilisateur=?, email=?, mot_de_passe=?, prenom=?, nom=? where id_utilisateur=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNomUtilisateur());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getMotDePasse());
        pst.setString(4, user.getPrenom());
        pst.setString(5, user.getNom());
        pst.setLong(6, user.getId());
        pst.executeUpdate();
        System.out.println("User modifie");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "delete from utilisateurs where id_utilisateur=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("User supprime");
    }

    @Override
    public List<User> afficher() throws SQLException {
        String req = "SELECT * FROM utilisateurs";
        List<User> users = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            User u = new User();
            u.setId(rs.getLong("id_utilisateur"));
            u.setNomUtilisateur(rs.getString("nom_utilisateur"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setPrenom(rs.getString("prenom"));
            u.setNom(rs.getString("nom"));
            u.setPhotoProfil(rs.getString("photo_profil"));
            u.setRole(rs.getString("role"));
            u.setEstActif(rs.getBoolean("est_actif"));
            u.setDateCreation(rs.getTimestamp("date_creation"));
            u.setDateModification(rs.getTimestamp("date_modification"));
            users.add(u);
        }
        return users;
    }
}