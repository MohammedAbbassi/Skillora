package Services;

import Entities.Reservation;
import Interffaces.InterfaceCRUD;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationCRUD implements InterfaceCRUD<Reservation> {

    private Connection conn;

    public ReservationCRUD() {
        conn = DBConnection.getConnection();
    }

    @Override
    public void ajouter(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (nb_places, date_reservation, id_evenement, id_utilisateur) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, reservation.getNb_places());
        ps.setDate(2, reservation.getDate_reservation());
        ps.setInt(3, reservation.getId_evenement());
        ps.setLong(4, reservation.getId_utilisateur());

        ps.executeUpdate();
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        List<Reservation> list = new ArrayList<>();

        String sql = "SELECT * FROM reservation";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setId_reservation(rs.getInt("id_reservation"));
            r.setNb_places(rs.getInt("nb_places"));
            r.setDate_reservation(rs.getDate("date_reservation"));
            r.setId_evenement(rs.getInt("id_evenement"));
            r.setId_utilisateur(rs.getLong("id_utilisateur"));

            list.add(r);
        }

        return list;
    }

    @Override
    public void modifier(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservation SET nb_places=?, date_reservation=?, id_evenement=?, id_utilisateur=? WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, reservation.getNb_places());
        ps.setDate(2, reservation.getDate_reservation());
        ps.setInt(3, reservation.getId_evenement());
        ps.setLong(4, reservation.getId_utilisateur());
        ps.setInt(5, reservation.getId_reservation());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
    }

    @Override
    public Reservation rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id_reservation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Reservation r = new Reservation();
            r.setId_reservation(rs.getInt("id_reservation"));
            r.setNb_places(rs.getInt("nb_places"));
            r.setDate_reservation(rs.getDate("date_reservation"));
            r.setId_evenement(rs.getInt("id_evenement"));
            r.setId_utilisateur(rs.getLong("id_utilisateur"));
            return r;
        }

        return null;
    }
}