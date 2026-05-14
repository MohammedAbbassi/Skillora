package com.skillora.shop.entities;

import java.time.LocalDateTime;

public class Coupon {
    public enum TypeReduction { POURCENTAGE, FIXE }

    private Long idCoupon;
    private String code;
    private TypeReduction typeReduction;
    private double valeurReduction;
    private LocalDateTime dateExpiration;
    private int maxUtilisations;
    private int nombreUtilisations;
    private double montantMinimum;
    private boolean actif;
    private LocalDateTime dateCreation;

    public Coupon() {}

    // Getters and Setters
    public Long getIdCoupon() { return idCoupon; }
    public void setIdCoupon(Long idCoupon) { this.idCoupon = idCoupon; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public TypeReduction getTypeReduction() { return typeReduction; }
    public void setTypeReduction(TypeReduction typeReduction) { this.typeReduction = typeReduction; }

    public double getValeurReduction() { return valeurReduction; }
    public void setValeurReduction(double valeurReduction) { this.valeurReduction = valeurReduction; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public int getMaxUtilisations() { return maxUtilisations; }
    public void setMaxUtilisations(int maxUtilisations) { this.maxUtilisations = maxUtilisations; }

    public int getNombreUtilisations() { return nombreUtilisations; }
    public void setNombreUtilisations(int nombreUtilisations) { this.nombreUtilisations = nombreUtilisations; }

    public double getMontantMinimum() { return montantMinimum; }
    public void setMontantMinimum(double montantMinimum) { this.montantMinimum = montantMinimum; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
