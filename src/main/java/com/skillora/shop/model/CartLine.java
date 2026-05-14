package com.skillora.shop.model;

import com.skillora.shop.entities.Produit;
import com.skillora.shop.MoneyFormat;

public class CartLine {
    private final long idProduit;
    private final String nom;
    private final double prixUnitaire;
    private int quantite;

    public CartLine(long idProduit, String nom, double prixUnitaire, int quantite) {
        this.idProduit = idProduit;
        this.nom = nom;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
    }

    public static CartLine fromProduit(Produit p, int quantite) {
        return new CartLine(p.getId(), p.getNom(), p.getPrix(), quantite);
    }

    public long getIdProduit() {
        return idProduit;
    }

    public String getNom() {
        return nom;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getSousTotal() {
        return prixUnitaire * quantite;
    }

    @Override
    public String toString() {
        return nom + " × " + quantite + "  →  " + MoneyFormat.amount(getSousTotal());
    }
}
