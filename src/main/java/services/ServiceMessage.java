package services;

import entities.Message;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage {
    private final Connection cnx;

    public ServiceMessage() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx != null) {
            autoMigrate();
        }
    }

    private void autoMigrate() {
        try {
            DatabaseMetaData meta = cnx.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "messages", null)) {
                if (!rs.next()) {
                    try (Statement st = cnx.createStatement()) {
                        st.executeUpdate("CREATE TABLE messages (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                "expediteur_id BIGINT NOT NULL," +
                                "destinataire_id BIGINT NOT NULL," +
                                "contenu TEXT NOT NULL," +
                                "date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                "est_lu BOOLEAN DEFAULT FALSE," +
                                "FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE," +
                                "FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE" +
                                ")");
                    }
                    System.out.println("Auto-migrated database: created messages table.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Auto-migration for messages failed: " + e.getMessage());
        }
    }

    public void envoyerMessage(Message message) throws SQLException {
        String req = "INSERT INTO messages(expediteur_id, destinataire_id, contenu) VALUES(?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, message.getExpediteurId());
            pst.setInt(2, message.getDestinataireId());
            pst.setString(3, message.getContenu());
            pst.executeUpdate();
        }
    }

    public List<Message> getMessagesEntre(int userId1, int userId2) throws SQLException {
        List<Message> list = new ArrayList<>();
        String req = "SELECT * FROM messages WHERE " +
                "(expediteur_id = ? AND destinataire_id = ?) OR " +
                "(expediteur_id = ? AND destinataire_id = ?) " +
                "ORDER BY date_envoi ASC";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId1);
            pst.setInt(2, userId2);
            pst.setInt(3, userId2);
            pst.setInt(4, userId1);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMessage(rs));
                }
            }
        }
        return list;
    }

    public List<Message> getUnreadMessages(int destinataireId) throws SQLException {
        List<Message> list = new ArrayList<>();
        String req = "SELECT m.*, COALESCE(u.prenom, u.nom_utilisateur, 'User') as expediteur_prenom, " +
                "COALESCE(u.nom, '') as expediteur_nom " +
                "FROM messages m " +
                "JOIN utilisateurs u ON m.expediteur_id = u.id_utilisateur " +
                "WHERE destinataire_id = ? AND est_lu = FALSE " +
                "ORDER BY date_envoi DESC";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, destinataireId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Message m = mapMessage(rs);
                    String prenom = rs.getString("expediteur_prenom");
                    String nom = rs.getString("expediteur_nom");
                    m.setExpediteurNom(nom.isEmpty() ? prenom : prenom + " " + nom);
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void markAsRead(int messageId) throws SQLException {
        String req = "UPDATE messages SET est_lu = TRUE WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, messageId);
            pst.executeUpdate();
        }
    }

    public void markAllAsRead(int userId) throws SQLException {
        String req = "UPDATE messages SET est_lu = TRUE WHERE destinataire_id = ? AND est_lu = FALSE";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setExpediteurId(rs.getInt("expediteur_id"));
        m.setDestinataireId(rs.getInt("destinataire_id"));
        m.setContenu(rs.getString("contenu"));
        m.setDateEnvoi(rs.getTimestamp("date_envoi"));
        m.setEstLu(rs.getBoolean("est_lu"));
        return m;
    }
}
