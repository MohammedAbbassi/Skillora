package Services;

import Entities.Coupon;
import Entities.CouponUsage;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {
    private final Connection conn = MyDatabase.getInstance().getCnx();

    public Coupon getByCode(String code) throws SQLException {
        String sql = "SELECT * FROM coupon WHERE code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    public Coupon getByOrderId(long idCommande) throws SQLException {
        String sql = "SELECT c.* FROM coupon c " +
                     "INNER JOIN coupon_usage cu ON c.id_coupon = cu.id_coupon " +
                     "WHERE cu.id_commande = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCommande);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    public void incrementUsage(Long idCoupon) throws SQLException {
        String sql = "UPDATE coupon SET nombre_utilisations = nombre_utilisations + 1 WHERE id_coupon = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCoupon);
            ps.executeUpdate();
        }
    }

    public void saveUsage(CouponUsage usage) throws SQLException {
        String sql = "INSERT INTO coupon_usage (id_coupon, id_utilisateur, id_commande, date_utilisation) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usage.getIdCoupon());
            ps.setLong(2, usage.getIdUtilisateur());
            ps.setLong(3, usage.getIdCommande());
            ps.executeUpdate();
        }
    }

    private Coupon mapCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setIdCoupon(rs.getLong("id_coupon"));
        c.setCode(rs.getString("code"));
        c.setTypeReduction(Coupon.TypeReduction.valueOf(rs.getString("type_reduction")));
        c.setValeurReduction(rs.getDouble("valeur_reduction"));
        c.setDateExpiration(rs.getTimestamp("date_expiration").toLocalDateTime());
        c.setMaxUtilisations(rs.getInt("max_utilisations"));
        c.setNombreUtilisations(rs.getInt("nombre_utilisations"));
        c.setMontantMinimum(rs.getDouble("montant_minimum"));
        c.setActif(rs.getBoolean("actif"));
        c.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return c;
    }
}
