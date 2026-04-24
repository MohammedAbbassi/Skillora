package Services;

import Entities.Evenement;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementCRUD {

    private Connection conn;

    public EvenementCRUD() {
        conn = DBConnection.getConnection();
    }

    public void ajouter(Evenement evenement) throws SQLException {
        String sql = "INSERT INTO evenement (nom, date_evenement, lieu, image, duree_minutes, id_utilisateur) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, evenement.getNom());
        ps.setDate(2, evenement.getDate_evenement());
        ps.setString(3, evenement.getLieu());
        ps.setString(4, evenement.getImage());
        ps.setInt(5, evenement.getDuree_minutes());
        ps.setLong(6, evenement.getId_utilisateur());

        ps.executeUpdate();
    }

    public List<Evenement> afficher() throws SQLException {
        List<Evenement> list = new ArrayList<>();

        String sql = "SELECT * FROM evenement";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Evenement e = new Evenement();
            e.setId_evenement(rs.getInt("id_evenement"));
            e.setNom(rs.getString("nom"));
            e.setDate_evenement(rs.getDate("date_evenement"));
            e.setLieu(rs.getString("lieu"));
            e.setImage(rs.getString("image"));
            e.setDuree_minutes(rs.getInt("duree_minutes"));
            e.setId_utilisateur(rs.getLong("id_utilisateur"));

            list.add(e);
        }

        return list;
    }

    public void modifier(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenement SET nom=?, date_evenement=?, lieu=?, image=?, duree_minutes=?, id_utilisateur=? WHERE id_evenement=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, evenement.getNom());
        ps.setDate(2, evenement.getDate_evenement());
        ps.setString(3, evenement.getLieu());
        ps.setString(4, evenement.getImage());
        ps.setInt(5, evenement.getDuree_minutes());
        ps.setLong(6, evenement.getId_utilisateur());
        ps.setInt(7, evenement.getId_evenement());

        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evenement WHERE id_evenement=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
    }

    public Evenement rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id_evenement=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Evenement e = new Evenement();
            e.setId_evenement(rs.getInt("id_evenement"));
            e.setNom(rs.getString("nom"));
            e.setDate_evenement(rs.getDate("date_evenement"));
            e.setLieu(rs.getString("lieu"));
            e.setImage(rs.getString("image"));
            e.setDuree_minutes(rs.getInt("duree_minutes"));
            e.setId_utilisateur(rs.getLong("id_utilisateur"));
            return e;
        }

        return null;
    }
}