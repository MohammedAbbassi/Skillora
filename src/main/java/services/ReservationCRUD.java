package services;

import entities.Reservation;
import interfaces.InterfaceCRUD;
import utils.DBConnection;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReservationCRUD implements InterfaceCRUD<Reservation> {

    private Connection conn;

    public ReservationCRUD() {
        conn = DBConnection.getConnection();
    }

    private void verifierConnexionBase() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DBConnection.getConnection();
        }
        if (conn == null) {
            throw new SQLException("La connexion a la base de donnees a echoue. Verifiez votre serveur MySQL.");
        }
        verifierColonneStatut();
        verifierColonneChaises();
    }

    private void verifierColonneStatut() throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, "reservation", "statut")) {
            if (!columns.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE reservation ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'");
                }
            }
        }
    }

    private void verifierColonneChaises() throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, "reservation", "chaises")) {
            if (!columns.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE reservation ADD COLUMN chaises VARCHAR(255) DEFAULT NULL");
                }
            }
        }
    }

    @Override
    public void ajouter(Reservation reservation) throws SQLException {
        verifierConnexionBase();
        if (existeDeja(reservation.getId_evenement(), reservation.getId_utilisateur())) {
            throw new SQLException("Vous avez deja reserve cet evenement.");
        }
        if (existeReservationActiveMemeDateEvenement(reservation.getId_evenement(), reservation.getId_utilisateur(), 0)) {
            throw new SQLException("Vous avez deja une reservation active pour un evenement a cette meme date.");
        }

        String sql = "INSERT INTO reservation (nb_places, date_reservation, id_evenement, id_utilisateur, chaises) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, reservation.getNb_places());
        ps.setDate(2, reservation.getDate_reservation());
        ps.setInt(3, reservation.getId_evenement());
        ps.setLong(4, reservation.getId_utilisateur());
        ps.setString(5, reservation.getChaises());

        ps.executeUpdate();
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        verifierConnexionBase();
        List<Reservation> list = new ArrayList<>();

        String sql = "SELECT r.*, e.nom AS nom_evenement, e.image AS image_evenement, e.lieu AS lieu_evenement, "
                + "e.id_utilisateur AS id_organisateur_evenement, "
                + "COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))), ''), u.nom_utilisateur) AS nom_utilisateur "
                + "FROM reservation r "
                + "LEFT JOIN evenement e ON e.id_evenement = r.id_evenement "
                + "LEFT JOIN utilisateurs u ON u.id_utilisateur = r.id_utilisateur";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(transformerResultatEnReservation(rs));
        }

        return list;
    }

    @Override
    public void modifier(Reservation reservation) throws SQLException {
        verifierConnexionBase();
        if (existeDejaPourAutreReservation(reservation.getId_evenement(), reservation.getId_utilisateur(), reservation.getId_reservation())) {
            throw new SQLException("Vous avez deja reserve cet evenement.");
        }
        if (existeReservationActiveMemeDateEvenement(reservation.getId_evenement(), reservation.getId_utilisateur(), reservation.getId_reservation())) {
            throw new SQLException("Vous avez deja une reservation active pour un evenement a cette meme date.");
        }

        String sql = "UPDATE reservation SET nb_places=?, date_reservation=?, id_evenement=?, id_utilisateur=?, chaises=? WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, reservation.getNb_places());
        ps.setDate(2, reservation.getDate_reservation());
        ps.setInt(3, reservation.getId_evenement());
        ps.setLong(4, reservation.getId_utilisateur());
        ps.setString(5, reservation.getChaises());
        ps.setInt(6, reservation.getId_reservation());

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Reservation introuvable.");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        verifierConnexionBase();
        String sql = "DELETE FROM reservation WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Reservation introuvable.");
        }
    }

    public void changerStatut(int idReservation, String statut) throws SQLException {
        verifierConnexionBase();
        String sql = "UPDATE reservation SET statut=? WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setInt(2, idReservation);

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Reservation introuvable.");
        }
    }

    public boolean existeDeja(int idEvenement, long idUtilisateur) throws SQLException {
        verifierConnexionBase();
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_evenement=? AND id_utilisateur=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEvenement);
        ps.setLong(2, idUtilisateur);

        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    public Set<String> recupererChaisesReservees(int idEvenement, int idReservationAExclure) throws SQLException {
        verifierConnexionBase();
        Set<String> chaisesReservees = new LinkedHashSet<>();
        String sql = "SELECT chaises FROM reservation "
                + "WHERE id_evenement=? "
                + "AND id_reservation<>? "
                + "AND statut <> 'REFUSEE' "
                + "AND chaises IS NOT NULL "
                + "AND TRIM(chaises) <> ''";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEvenement);
        ps.setInt(2, idReservationAExclure);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String chaises = rs.getString("chaises");
            for (String chaise : chaises.split(",")) {
                String code = chaise.trim();
                if (!code.isEmpty()) {
                    chaisesReservees.add(code);
                }
            }
        }

        return chaisesReservees;
    }

    private boolean existeDejaPourAutreReservation(int idEvenement, long idUtilisateur, int idReservation) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_evenement=? AND id_utilisateur=? AND id_reservation<>?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEvenement);
        ps.setLong(2, idUtilisateur);
        ps.setInt(3, idReservation);

        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private boolean existeReservationActiveMemeDateEvenement(int idEvenement, long idUtilisateur, int idReservationAExclure) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM reservation r "
                + "JOIN evenement evenement_reserve ON evenement_reserve.id_evenement = r.id_evenement "
                + "JOIN evenement nouvel_evenement ON nouvel_evenement.id_evenement = ? "
                + "WHERE r.id_utilisateur = ? "
                + "AND r.id_reservation <> ? "
                + "AND r.statut <> 'REFUSEE' "
                + "AND evenement_reserve.date_evenement = nouvel_evenement.date_evenement";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEvenement);
        ps.setLong(2, idUtilisateur);
        ps.setInt(3, idReservationAExclure);

        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    @Override
    public Reservation rechercherParId(int id) throws SQLException {
        verifierConnexionBase();
        String sql = "SELECT r.*, e.nom AS nom_evenement, e.image AS image_evenement, e.lieu AS lieu_evenement, "
                + "e.id_utilisateur AS id_organisateur_evenement, "
                + "COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))), ''), u.nom_utilisateur) AS nom_utilisateur "
                + "FROM reservation r "
                + "LEFT JOIN evenement e ON e.id_evenement = r.id_evenement "
                + "LEFT JOIN utilisateurs u ON u.id_utilisateur = r.id_utilisateur "
                + "WHERE r.id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return transformerResultatEnReservation(rs);
        }

        return null;
    }

    private Reservation transformerResultatEnReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId_reservation(rs.getInt("id_reservation"));
        r.setNb_places(rs.getInt("nb_places"));
        r.setDate_reservation(rs.getDate("date_reservation"));
        r.setId_evenement(rs.getInt("id_evenement"));
        r.setId_utilisateur(rs.getLong("id_utilisateur"));
        r.setNom_evenement(rs.getString("nom_evenement"));
        r.setImage_evenement(rs.getString("image_evenement"));
        r.setLieu_evenement(rs.getString("lieu_evenement"));
        r.setId_organisateur_evenement(rs.getLong("id_organisateur_evenement"));
        r.setNom_utilisateur(rs.getString("nom_utilisateur"));
        r.setStatut(rs.getString("statut"));
        r.setChaises(rs.getString("chaises"));
        return r;
    }
}

