package Entities;

public class Question {

    private int id;
    private String libelle;
    private String niveau;
    private int score;
    private String categorie;
    private String auteur;
    private boolean active;

    public Question() {
    }

    public Question(String libelle, String niveau, int score, String categorie, String auteur, boolean active) {
        this.libelle = libelle;
        this.niveau = niveau;
        this.score = score;
        this.categorie = categorie;
        this.auteur = auteur;
        this.active = active;
    }

    public Question(int id, String libelle, String niveau, int score, String categorie, String auteur, boolean active) {
        this.id = id;
        this.libelle = libelle;
        this.niveau = niveau;
        this.score = score;
        this.categorie = categorie;
        this.auteur = auteur;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", niveau='" + niveau + '\'' +
                ", score=" + score +
                ", categorie='" + categorie + '\'' +
                ", auteur='" + auteur + '\'' +
                ", active=" + active +
                '}';
    }
}