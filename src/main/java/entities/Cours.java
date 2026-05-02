package entities;

import java.time.LocalDate;

public class Cours {

    private int idCours;
    private String titre;
    private String description;
    private String domaine;
    private String niveau;
    private int duree;
    private LocalDate dateCreation;
    private Long idInstructeur;
    private int progression;

    // ─── Constructeurs ───────────────────────────────────────────────────────────

    public Cours() {}

    /** Constructeur sans id (pour l'ajout) */
    public Cours(String titre, String description, String domaine,
                 String niveau, int duree, LocalDate dateCreation,
                 Long idInstructeur) {
        this.titre        = titre;
        this.description  = description;
        this.domaine      = domaine;
        this.niveau       = niveau;
        this.duree        = duree;
        this.dateCreation = dateCreation;
        this.idInstructeur = idInstructeur;
        this.progression  = 0;
    }

    /** Constructeur complet (pour la lecture en BD) */
    public Cours(int idCours, String titre, String description, String domaine,
                 String niveau, int duree, LocalDate dateCreation,
                 Long idInstructeur, int progression) {
        this.idCours      = idCours;
        this.titre        = titre;
        this.description  = description;
        this.domaine      = domaine;
        this.niveau       = niveau;
        this.duree        = duree;
        this.dateCreation = dateCreation;
        this.idInstructeur = idInstructeur;
        this.progression  = progression;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────────

    public int getIdCours()                     { return idCours; }
    public void setIdCours(int idCours)          { this.idCours = idCours; }

    public String getTitre()                    { return titre; }
    public void setTitre(String titre)           { this.titre = titre; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)      { this.description = desc; }

    public String getDomaine()                  { return domaine; }
    public void setDomaine(String domaine)       { this.domaine = domaine; }

    public String getNiveau()                   { return niveau; }
    public void setNiveau(String niveau)         { this.niveau = niveau; }

    public int getDuree()                       { return duree; }
    public void setDuree(int duree)              { this.duree = duree; }

    public LocalDate getDateCreation()          { return dateCreation; }
    public void setDateCreation(LocalDate d)     { this.dateCreation = d; }

    public Long getIdInstructeur()              { return idInstructeur; }
    public void setIdInstructeur(Long idInstructeur) { this.idInstructeur = idInstructeur; }

    public int getProgression()                 { return progression; }
    public void setProgression(int progression)  { this.progression = progression; }

    // ─── toString ────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Cours{" +
                "idCours=" + idCours +
                ", titre='" + titre + '\'' +
                ", domaine='" + domaine + '\'' +
                ", niveau='" + niveau + '\'' +
                ", duree=" + duree + "min" +
                ", idInstructeur=" + idInstructeur +
                ", progression=" + progression + "%" +
                '}';
    }
}
