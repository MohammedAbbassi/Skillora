package entities;

import java.util.Date;

public class Cours {

    private int id_cours;
    private String titre;
    private String description;
    private String domaine;
    private String niveau;
    private int duree;
    private Date date_creation;
    private String instructeur;

    // Constructeur vide
    public Cours() {}

    // Constructeur complet
    public Cours(int id_cours, String titre, String description, String domaine,
                 String niveau, int duree, Date date_creation, String instructeur) {
        this.id_cours = id_cours;
        this.titre = titre;
        this.description = description;
        this.domaine = domaine;
        this.niveau = niveau;
        this.duree = duree;
        this.date_creation = date_creation;
        this.instructeur = instructeur;
    }

    // Constructeur sans id (pour insertion)
    public Cours(String titre, String description, String domaine,
                 String niveau, int duree, Date date_creation, String instructeur) {
        this.titre = titre;
        this.description = description;
        this.domaine = domaine;
        this.niveau = niveau;
        this.duree = duree;
        this.date_creation = date_creation;
        this.instructeur = instructeur;
    }

    // Getters & Setters

    public int getId_cours() {
        return id_cours;
    }

    public void setId_cours(int id_cours) {
        this.id_cours = id_cours;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public Date getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Date date_creation) {
        this.date_creation = date_creation;
    }

    public String getInstructeur() {
        return instructeur;
    }

    public void setInstructeur(String instructeur) {
        this.instructeur = instructeur;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id_cours=" + id_cours +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", domaine='" + domaine + '\'' +
                ", niveau='" + niveau + '\'' +
                ", duree=" + duree +
                ", date_creation=" + date_creation +
                ", instructeur='" + instructeur + '\'' +
                '}';
    }
}