package entities;

import java.time.LocalDateTime;

public class CommandeRecord {
    private long idCommande;
    private LocalDateTime dateCommande;
    private double total;
    private String statut;
    private long idUtilisateur;

    public CommandeRecord() {
    }

    public CommandeRecord(long idCommande, LocalDateTime dateCommande, double total, String statut, long idUtilisateur) {
        this.idCommande = idCommande;
        this.dateCommande = dateCommande;
        this.total = total;
        this.statut = statut;
        this.idUtilisateur = idUtilisateur;
    }

    public long getIdCommande() {
        return idCommande;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public double getTotal() {
        return total;
    }

    public String getStatut() {
        return statut;
    }

    public long getIdUtilisateur() {
        return idUtilisateur;
    }
    
    public void setIdCommande(long idCommande) { this.idCommande = idCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    public void setTotal(double total) { this.total = total; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setIdUtilisateur(long idUtilisateur) { this.idUtilisateur = idUtilisateur; }
}
