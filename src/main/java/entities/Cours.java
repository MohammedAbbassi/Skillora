package entities;

import java.time.LocalDate;

public class Cours {

    private int idCours;
    private String titre;
    private String description;
    private String categorie;
    private String niveau;
    private String duree; // ex: "2h"
    private String objectifSemaine;
    private String performance;
    private LocalDate dateCreation;
    private int progression;

    // ─── Constructeurs ───────────────────────────────────────────────────────────

    public Cours() {}

    public Cours(String titre, String description, String categorie, String niveau, 
                 String duree, String objectifSemaine, String performance, LocalDate dateCreation) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.niveau = niveau;
        this.duree = duree;
        this.objectifSemaine = objectifSemaine;
        this.performance = performance;
        this.dateCreation = dateCreation;
        this.progression = 0;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────────

    public int getIdCours() { return idCours; }
    public void setIdCours(int idCours) { this.idCours = idCours; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public String getObjectifSemaine() { return objectifSemaine; }
    public void setObjectifSemaine(String objectifSemaine) { this.objectifSemaine = objectifSemaine; }

    public String getPerformance() { return performance; }
    public void setPerformance(String performance) { this.performance = performance; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public int getProgression() { return progression; }
    public void setProgression(int progression) { this.progression = progression; }

    @Override
    public String toString() {
        return "Cours{" +
                "idCours=" + idCours +
                ", titre='" + titre + '\'' +
                ", duree='" + duree + '\'' +
                ", progression=" + progression + "%" +
                '}';
    }
}

