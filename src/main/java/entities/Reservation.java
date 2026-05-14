package entities;

import java.sql.Date;

public class Reservation {
    private int id_reservation;
    private int nb_places;
    private Date date_reservation;
    private int id_evenement;
    private long id_utilisateur;
    private String nom_evenement;
    private String image_evenement;
    private String lieu_evenement;
    private long id_organisateur_evenement;
    private String nom_utilisateur;
    private String statut = "EN_ATTENTE";
    private String chaises;

    public Reservation() {
    }

    public Reservation(int nb_places, Date date_reservation, int id_evenement, long id_utilisateur) {
        this.nb_places = nb_places;
        this.date_reservation = date_reservation;
        this.id_evenement = id_evenement;
        this.id_utilisateur = id_utilisateur;
    }

    public int getId_reservation() {
        return id_reservation;
    }

    public void setId_reservation(int id_reservation) {
        this.id_reservation = id_reservation;
    }

    public int getNb_places() {
        return nb_places;
    }

    public void setNb_places(int nb_places) {
        this.nb_places = nb_places;
    }

    public Date getDate_reservation() {
        return date_reservation;
    }

    public void setDate_reservation(Date date_reservation) {
        this.date_reservation = date_reservation;
    }

    public int getId_evenement() {
        return id_evenement;
    }

    public void setId_evenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    public long getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(long id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public String getNom_evenement() {
        return nom_evenement;
    }

    public void setNom_evenement(String nom_evenement) {
        this.nom_evenement = nom_evenement;
    }

    public String getImage_evenement() {
        return image_evenement;
    }

    public void setImage_evenement(String image_evenement) {
        this.image_evenement = image_evenement;
    }

    public String getLieu_evenement() {
        return lieu_evenement;
    }

    public void setLieu_evenement(String lieu_evenement) {
        this.lieu_evenement = lieu_evenement;
    }

    public long getId_organisateur_evenement() {
        return id_organisateur_evenement;
    }

    public void setId_organisateur_evenement(long id_organisateur_evenement) {
        this.id_organisateur_evenement = id_organisateur_evenement;
    }

    public String getNom_utilisateur() {
        return nom_utilisateur;
    }

    public void setNom_utilisateur(String nom_utilisateur) {
        this.nom_utilisateur = nom_utilisateur;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getChaises() {
        return chaises;
    }

    public void setChaises(String chaises) {
        this.chaises = chaises;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id_reservation +
                ", nb_places=" + nb_places +
                ", date_reservation=" + date_reservation +
                ", id_evenement=" + id_evenement +
                ", id_utilisateur=" + id_utilisateur +
                ", nom_utilisateur='" + nom_utilisateur + '\'' +
                ", nom_evenement='" + nom_evenement + '\'' +
                ", id_organisateur_evenement=" + id_organisateur_evenement +
                ", statut='" + statut + '\'' +
                ", chaises='" + chaises + '\'' +
                '}';
    }
}
