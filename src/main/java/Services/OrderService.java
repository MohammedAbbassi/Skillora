package Services;

import Entities.CommandeRecord;
import com.skillora.model.CartLine;
import com.skillora.model.OrderLine;
import com.skillora.model.OrderStatsSummary;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Commandes et lignes {@code commande_produit} — extrait sans logique cours / accès.
 */
public class OrderService {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public long placeOrder(long userId, List<CartLine> lines, String statutInitial) throws SQLException {
        return placeOrder(userId, lines, statutInitial, null);
    }

    public long placeOrder(long userId, List<CartLine> lines, String statutInitial, Double totalForce) throws SQLException {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Panier vide");
        }
        double total = totalForce != null ? totalForce : lines.stream().mapToDouble(CartLine::getSousTotal).sum();

        boolean prevAuto = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            String insCmd = "INSERT INTO commande (date_commande, total, statut, id_utilisateur) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(insCmd, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, total);
                ps.setString(2, statutInitial != null ? statutInitial : "EN_ATTENTE");
                ps.setLong(3, userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Clé commande introuvable");
                    }
                    long idCommande = keys.getLong(1);

                    String insLine = "INSERT INTO commande_produit (id_commande, id_produit, quantite, prix_unitaire) VALUES (?,?,?,?)";
                    try (PreparedStatement lp = cnx.prepareStatement(insLine)) {
                        for (CartLine line : lines) {
                            lp.setLong(1, idCommande);
                            lp.setInt(2, (int) line.getIdProduit());
                            lp.setInt(3, line.getQuantite());
                            lp.setDouble(4, line.getPrixUnitaire());
                            lp.addBatch();
                        }
                        lp.executeBatch();
                    }
                    cnx.commit();
                    return idCommande;
                }
            }
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(prevAuto);
        }
    }

    public List<CommandeRecord> listForUser(long userId) throws SQLException {
        List<CommandeRecord> list = new ArrayList<>();
        String sql = "SELECT id_commande, date_commande, total, statut, id_utilisateur FROM commande WHERE id_utilisateur = ? ORDER BY date_commande DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("date_commande");
                    LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
                    list.add(new CommandeRecord(
                            rs.getLong("id_commande"),
                            dt,
                            rs.getDouble("total"),
                            rs.getString("statut"),
                            rs.getLong("id_utilisateur")));
                }
            }
        }
        return list;
    }

    public List<CommandeRecord> listAll() throws SQLException {
        List<CommandeRecord> list = new ArrayList<>();
        String sql = "SELECT id_commande, date_commande, total, statut, id_utilisateur FROM commande ORDER BY date_commande DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_commande");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
                list.add(new CommandeRecord(
                        rs.getLong("id_commande"),
                        dt,
                        rs.getDouble("total"),
                        rs.getString("statut"),
                        rs.getLong("id_utilisateur")));
            }
        }
        return list;
    }

    public void updateStatut(long idCommande, String statut) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE commande SET statut = ? WHERE id_commande = ?")) {
            ps.setString(1, statut);
            ps.setLong(2, idCommande);
            ps.executeUpdate();
        }
    }

    /** Met à jour statut et total (administration). */
    public void updateCommandeAdmin(long idCommande, String statut, double total) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE commande SET statut = ?, total = ? WHERE id_commande = ?")) {
            ps.setString(1, statut);
            ps.setDouble(2, total);
            ps.setLong(3, idCommande);
            ps.executeUpdate();
        }
    }

    /** Supprime une commande et ses lignes (FK CASCADE sur commande_produit si défini). */
    public void deleteOrder(long idCommande) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM commande WHERE id_commande = ?")) {
            ps.setLong(1, idCommande);
            ps.executeUpdate();
        }
    }

    public List<OrderLine> listLines(long idCommande) throws SQLException {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT cp.id_produit, cp.quantite, cp.prix_unitaire, pr.nom FROM commande_produit cp "
                + "INNER JOIN produit pr ON pr.id_produit = cp.id_produit WHERE cp.id_commande = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, idCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new OrderLine(
                            rs.getInt("id_produit"),
                            rs.getString("nom"),
                            rs.getInt("quantite"),
                            rs.getDouble("prix_unitaire")));
                }
            }
        }
        return lines;
    }

    public OrderStatsSummary statsForUser(long userId) throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total),0), "
                + "SUM(CASE WHEN statut='EN_ATTENTE' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN statut='PAYEE' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN statut='ANNULEE' THEN 1 ELSE 0 END) "
                + "FROM commande WHERE id_utilisateur = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStats(rs);
                }
            }
        }
        return emptyStats();
    }

    public OrderStatsSummary statsAll() throws SQLException {
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT COUNT(*), COALESCE(SUM(total),0), "
                        + "SUM(CASE WHEN statut='EN_ATTENTE' THEN 1 ELSE 0 END), "
                        + "SUM(CASE WHEN statut='PAYEE' THEN 1 ELSE 0 END), "
                        + "SUM(CASE WHEN statut='ANNULEE' THEN 1 ELSE 0 END) FROM commande")) {
            if (rs.next()) {
                return mapStats(rs);
            }
        }
        return emptyStats();
    }

    private static OrderStatsSummary mapStats(ResultSet rs) throws SQLException {
        OrderStatsSummary s = new OrderStatsSummary();
        s.setOrderCount(rs.getInt(1));
        s.setTotalAmount(rs.getDouble(2));
        s.setEnAttente(rs.getInt(3));
        s.setPayee(rs.getInt(4));
        s.setAnnulee(rs.getInt(5));
        return s;
    }

    private static OrderStatsSummary emptyStats() {
        return new OrderStatsSummary();
    }
}
