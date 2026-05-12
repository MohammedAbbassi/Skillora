package Services;

import Entities.Coupon;
import Entities.CouponUsage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CouponService {
    private final CouponDAO couponDAO = new CouponDAO();

    public Coupon validateCoupon(String code, double montantTotal) throws Exception {
        Coupon coupon = couponDAO.getByCode(code);

        if (coupon == null) {
            throw new Exception("Code promo invalide.");
        }

        if (!coupon.isActif()) {
            throw new Exception("Ce coupon n'est plus actif.");
        }

        if (coupon.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new Exception("Ce coupon a expiré.");
        }

        if (coupon.getNombreUtilisations() >= coupon.getMaxUtilisations()) {
            throw new Exception("Ce coupon a atteint son nombre maximum d'utilisations.");
        }

        if (montantTotal < coupon.getMontantMinimum()) {
            throw new Exception("Le montant minimum pour utiliser ce coupon est de " + coupon.getMontantMinimum() + " TND.");
        }

        return coupon;
    }

    public double calculateDiscount(Coupon coupon, double montantTotal) {
        if (coupon.getTypeReduction() == Coupon.TypeReduction.POURCENTAGE) {
            return montantTotal * (coupon.getValeurReduction() / 100.0);
        } else {
            return Math.min(coupon.getValeurReduction(), montantTotal);
        }
    }

    public void registerUsage(Long idCoupon, Long idUtilisateur, Long idCommande) throws SQLException {
        CouponUsage usage = new CouponUsage();
        usage.setIdCoupon(idCoupon);
        usage.setIdUtilisateur(idUtilisateur);
        usage.setIdCommande(idCommande);
        
        couponDAO.saveUsage(usage);
        couponDAO.incrementUsage(idCoupon);
    }

    public Coupon getUsedCoupon(long idCommande) throws SQLException {
        return couponDAO.getByOrderId(idCommande);
    }
}
