package entities;

import java.sql.Timestamp;

public class Jaime {
    private int idPost;
    private long idUtilisateur;
    private Timestamp dateJaime;

    public Jaime() {
    }

    public Jaime(int idPost, long idUtilisateur) {
        this.idPost = idPost;
        this.idUtilisateur = idUtilisateur;
    }

    public Jaime(int idPost, long idUtilisateur, Timestamp dateJaime) {
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

    public long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(long idUtilisateur) {
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
