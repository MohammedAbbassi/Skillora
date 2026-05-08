package models;

public class OrderLine {
    private final int idProduit;
    private final String nomProduit;
    private final int quantite;
    private final double prixUnitaire;

    public OrderLine(int idProduit, String nomProduit, int quantite, double prixUnitaire) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
