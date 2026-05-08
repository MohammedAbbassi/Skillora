package services;

import entities.User;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRelation {
    private Connection cnx;

    public ServiceRelation() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public void addFriend(int requesterId, int receiverId) throws SQLException {
        String req = "INSERT INTO relations_utilisateurs (id_demandeur, id_receveur, statut) VALUES (?, ?, 'FRIEND') " +
                     "ON DUPLICATE KEY UPDATE statut = 'FRIEND'";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, requesterId);
        pst.setInt(2, receiverId);
        pst.executeUpdate();
    }

    public void blockUser(int requesterId, int receiverId) throws SQLException {
        String req = "INSERT INTO relations_utilisateurs (id_demandeur, id_receveur, statut) VALUES (?, ?, 'BLOCKED') " +
                     "ON DUPLICATE KEY UPDATE statut = 'BLOCKED'";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, requesterId);
        pst.setInt(2, receiverId);
        pst.executeUpdate();
    }

    public void removeRelation(int user1, int user2) throws SQLException {
        String req = "DELETE FROM relations_utilisateurs WHERE (id_demandeur = ? AND id_receveur = ?) OR (id_demandeur = ? AND id_receveur = ?)";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, user1);
        pst.setInt(2, user2);
        pst.setInt(3, user2);
        pst.setInt(4, user1);
        pst.executeUpdate();
    }

    public String getRelationStatus(int requesterId, int receiverId) throws SQLException {
        String req = "SELECT statut FROM relations_utilisateurs WHERE id_demandeur = ? AND id_receveur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, requesterId);
        pst.setInt(2, receiverId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return rs.getString("statut");
        
        // Check reverse for friendship
        req = "SELECT statut FROM relations_utilisateurs WHERE id_demandeur = ? AND id_receveur = ? AND statut = 'FRIEND'";
        pst = cnx.prepareStatement(req);
        pst.setInt(1, receiverId);
        pst.setInt(2, requesterId);
        rs = pst.executeQuery();
        if (rs.next()) return "FRIEND";

        return "NONE";
    }
    public List<Integer> getFriendsIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT id_receveur FROM relations_utilisateurs WHERE id_demandeur = ? AND statut = 'FRIEND' " +
                     "UNION " +
                     "SELECT id_demandeur FROM relations_utilisateurs WHERE id_receveur = ? AND statut = 'FRIEND'";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, userId);
        pst.setInt(2, userId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        return ids;
    }
}
