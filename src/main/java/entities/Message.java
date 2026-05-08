package entities;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int expediteurId;
    private int destinataireId;
    private String contenu;
    private Timestamp dateEnvoi;
    private boolean estLu;
    
    // Virtual fields for display
    private String expediteurNom;
    
    public Message() {}
    
    public Message(int expediteurId, int destinataireId, String contenu) {
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.contenu = contenu;
        this.estLu = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int expediteurId) { this.expediteurId = expediteurId; }
    
    public int getDestinataireId() { return destinataireId; }
    public void setDestinataireId(int destinataireId) { this.destinataireId = destinataireId; }
    
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    
    public Timestamp getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Timestamp dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    
    public boolean isEstLu() { return estLu; }
    public void setEstLu(boolean estLu) { this.estLu = estLu; }
    
    public String getExpediteurNom() { return expediteurNom; }
    public void setExpediteurNom(String expediteurNom) { this.expediteurNom = expediteurNom; }
}
