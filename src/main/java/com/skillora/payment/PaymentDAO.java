package com.skillora.payment;

import utils.MyDatabase;
import java.sql.*;

/**
 * DAO pour la table paiement.
 */
public class PaymentDAO {
    private final Connection conn = MyDatabase.getInstance().getCnx();

    public void savePayment(long idCommande, String paymentId, double montant, String statut) throws SQLException {
        String sql = "INSERT INTO paiement (id_commande, payment_id, montant, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCommande);
            ps.setString(2, paymentId);
            ps.setDouble(3, montant);
            ps.setString(4, statut);
            ps.executeUpdate();
        }
    }

    public void updatePaymentStatus(String paymentId, String status) throws SQLException {
        String sql = "UPDATE paiement SET statut = ? WHERE payment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, paymentId);
            ps.executeUpdate();
        }
    }
}
