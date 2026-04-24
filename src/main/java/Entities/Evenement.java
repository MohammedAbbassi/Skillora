package Entities;

import java.sql.Date;

public class Evenement {
    private int id_evenement;
    private String nom;
    private Date date_evenement;
    private String lieu;
    private String image;
    private int duree_minutes;
    private long id_utilisateur;

    public Evenement() {
    }

    public int getId_evenement() {
        return id_evenement;
    }

    public void setId_evenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Date getDate_evenement() {
        return date_evenement;
    }

    public void setDate_evenement(Date date_evenement) {
        this.date_evenement = date_evenement;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getDuree_minutes() {
        return duree_minutes;
    }

    public void setDuree_minutes(int duree_minutes) {
        this.duree_minutes = duree_minutes;
    }

    public long getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(long id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }
}