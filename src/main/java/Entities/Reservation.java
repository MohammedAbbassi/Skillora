package Entities;

import java.sql.Date;

public class Reservation {
    private int id_reservation;
    private int nb_places;
    private Date date_reservation;
    private int id_evenement;
    private long id_utilisateur;

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

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id_reservation +
                ", nb_places=" + nb_places +
                ", date_reservation=" + date_reservation +
                ", id_evenement=" + id_evenement +
                ", id_utilisateur=" + id_utilisateur +
                '}';
    }
}
