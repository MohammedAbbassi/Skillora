package entities;

import java.sql.Timestamp;

public class Jaime {
    private int idPost;
    private int idUtilisateur;
    private Timestamp dateJaime;

    public Jaime() {
    }

    public Jaime(int idPost, int idUtilisateur) {
        this.idPost = idPost;
        this.idUtilisateur = idUtilisateur;
    }

    public Jaime(int idPost, int idUtilisateur, Timestamp dateJaime) {
        this.idPost = idPost;
        this.idUtilisateur = idUtilisateur;
        this.dateJaime = dateJaime;
    }

    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Timestamp getDateJaime() {
        return dateJaime;
    }

    public void setDateJaime(Timestamp dateJaime) {
        this.dateJaime = dateJaime;
    }

    @Override
    public String toString() {
        return "Jaime{" +
                "idPost=" + idPost +
                ", idUtilisateur=" + idUtilisateur +
                ", dateJaime=" + dateJaime +
                '}';
    }
}
