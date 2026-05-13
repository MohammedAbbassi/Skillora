package services;

import entities.Notification;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements IService<Notification> {

    private final Connection cnx;

    public NotificationService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Notification notification) throws SQLException {
        String req = "INSERT INTO `notification`(`type`, `message`, `lu`, `id_utilisateur`, `id_declencheur`, `id_post`, `id_commentaire`) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, notification.getType());
        pst.setString(2, notification.getMessage());
        pst.setBoolean(3, notification.isLu());
        pst.setLong(4, notification.getIdUtilisateur());
        if (notification.getIdDeclencheur() != null) {
            pst.setLong(5, notification.getIdDeclencheur());
        } else {
            pst.setNull(5, Types.BIGINT);
        }
        if (notification.getIdPost() != null) {
            pst.setInt(6, notification.getIdPost());
        } else {
            pst.setNull(6, Types.INTEGER);
        }
        if (notification.getIdCommentaire() != null) {
            pst.setInt(7, notification.getIdCommentaire());
        } else {
            pst.setNull(7, Types.INTEGER);
        }
        pst.executeUpdate();
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            notification.setIdNotification(rs.getLong(1));
        }
    }

    @Override
    public void update(Notification notification) throws SQLException {
        String req = "UPDATE notification SET lu = ? WHERE id_notification = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setBoolean(1, notification.isLu());
        pst.setLong(2, notification.getIdNotification());
        pst.executeUpdate();
    }

    @Override
    public void delete(Notification notification) throws SQLException {
        String req = "DELETE FROM notification WHERE id_notification = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, notification.getIdNotification());
        pst.executeUpdate();
    }

    @Override
    public List<Notification> getAll() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String req = "SELECT * FROM notification ORDER BY date_creation DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            notifications.add(mapRow(rs));
        }
        return notifications;
    }

    public List<Notification> getByUser(long idUtilisateur) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String req = "SELECT * FROM notification WHERE id_utilisateur = ? ORDER BY date_creation DESC";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, idUtilisateur);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            notifications.add(mapRow(rs));
        }
        return notifications;
    }

    public List<Notification> getUnreadByUser(long idUtilisateur) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String req = "SELECT * FROM notification WHERE id_utilisateur = ? AND lu = 0 ORDER BY date_creation DESC";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, idUtilisateur);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            notifications.add(mapRow(rs));
        }
        return notifications;
    }

    public int getUnreadCount(long idUtilisateur) throws SQLException {
        String req = "SELECT COUNT(*) FROM notification WHERE id_utilisateur = ? AND lu = 0";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, idUtilisateur);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public void markAsRead(long idNotification) throws SQLException {
        String req = "UPDATE notification SET lu = 1 WHERE id_notification = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, idNotification);
        pst.executeUpdate();
    }

    public void markAllAsRead(long idUtilisateur) throws SQLException {
        String req = "UPDATE notification SET lu = 1 WHERE id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setLong(1, idUtilisateur);
        pst.executeUpdate();
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setIdNotification(rs.getLong("id_notification"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setLu(rs.getBoolean("lu"));
        n.setDateCreation(rs.getTimestamp("date_creation"));
        n.setIdUtilisateur(rs.getLong("id_utilisateur"));
        long declencheur = rs.getLong("id_declencheur");
        n.setIdDeclencheur(rs.wasNull() ? null : declencheur);
        int post = rs.getInt("id_post");
        n.setIdPost(rs.wasNull() ? null : post);
        int commentaire = rs.getInt("id_commentaire");
        n.setIdCommentaire(rs.wasNull() ? null : commentaire);
        return n;
    }
}
