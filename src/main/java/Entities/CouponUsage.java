package Entities;

import java.time.LocalDateTime;

public class CouponUsage {
    private Long idUsage;
    private Long idCoupon;
    private Long idUtilisateur;
    private Long idCommande;
    private LocalDateTime dateUtilisation;

    public CouponUsage() {}

    // Getters and Setters
    public Long getIdUsage() { return idUsage; }
    public void setIdUsage(Long idUsage) { this.idUsage = idUsage; }

    public Long getIdCoupon() { return idCoupon; }
    public void setIdCoupon(Long idCoupon) { this.idCoupon = idCoupon; }

    public Long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Long idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Long getIdCommande() { return idCommande; }
    public void setIdCommande(Long idCommande) { this.idCommande = idCommande; }

    public LocalDateTime getDateUtilisation() { return dateUtilisation; }
    public void setDateUtilisation(LocalDateTime dateUtilisation) { this.dateUtilisation = dateUtilisation; }
}
