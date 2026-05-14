package services;

import entities.Evenement;
import interfaces.InterfaceCRUD;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementCRUD implements InterfaceCRUD<Evenement> {

    private Connection conn;

    public EvenementCRUD() {
        conn = DBConnection.getConnection();
    }

    private void verifierConnexionBase() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DBConnection.getConnection();
        }
        if (conn == null) {
            throw new SQLException("La connexion a la base de donnees a echoue. Verifiez votre serveur MySQL.");
        }
    }

    public void ajouter(Evenement evenement) throws SQLException {
        verifierConnexionBase();
        if (existeEvenementIdentique(evenement, 0)) {
            throw new SQLException("Cet evenement existe deja avec le meme nom, la meme date et le meme lieu.");
        }

        String sql = "INSERT INTO evenement (nom, date_evenement, lieu, image, duree_minutes, id_utilisateur) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, evenement.getNom());
        ps.setDate(2, evenement.getDate_evenement());
        ps.setString(3, evenement.getLieu());
        ps.setString(4, evenement.getImage());
        ps.setInt(5, evenement.getDuree_minutes());
        ps.setLong(6, evenement.getId_utilisateur());

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Evenement non ajoute.");
        }
    }

    public List<Evenement> afficher() throws SQLException {
        verifierConnexionBase();
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
        verifierConnexionBase();
        if (existeEvenementIdentique(evenement, evenement.getId_evenement())) {
            throw new SQLException("Un autre evenement existe deja avec le meme nom, la meme date et le meme lieu.");
        }

        String sql = "UPDATE evenement SET nom=?, date_evenement=?, lieu=?, image=?, duree_minutes=?, id_utilisateur=? WHERE id_evenement=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, evenement.getNom());
        ps.setDate(2, evenement.getDate_evenement());
        ps.setString(3, evenement.getLieu());
        ps.setString(4, evenement.getImage());
        ps.setInt(5, evenement.getDuree_minutes());
        ps.setLong(6, evenement.getId_utilisateur());
        ps.setInt(7, evenement.getId_evenement());

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Evenement introuvable.");
        }
    }

    public void supprimer(int id) throws SQLException {
        verifierConnexionBase();
        String sql = "DELETE FROM evenement WHERE id_evenement=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Evenement introuvable.");
        }
    }

    public Evenement rechercherParId(int id) throws SQLException {
        verifierConnexionBase();
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

    private boolean existeEvenementIdentique(Evenement evenement, int idAExclure) throws SQLException {
        verifierConnexionBase();
        String sql = "SELECT COUNT(*) FROM evenement "
                + "WHERE LOWER(TRIM(nom)) = LOWER(TRIM(?)) "
                + "AND date_evenement = ? "
                + "AND LOWER(TRIM(lieu)) = LOWER(TRIM(?)) "
                + "AND id_evenement <> ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, evenement.getNom());
        ps.setDate(2, evenement.getDate_evenement());
        ps.setString(3, evenement.getLieu());
        ps.setInt(4, idAExclure);

        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
