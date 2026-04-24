package entities;

import java.sql.Timestamp;

public class Commentaire {
    private int idCommentaire;
    private String contenu;
    private Timestamp dateCreation;
    private long idUtilisateur;
    private int idPost;

    public Commentaire() {
    }

    public Commentaire(String contenu, long idUtilisateur, int idPost) {
        this.contenu = contenu;
        this.idUtilisateur = idUtilisateur;
        this.idPost = idPost;
    }

    public Commentaire(int idCommentaire, String contenu, Timestamp dateCreation, long idUtilisateur, int idPost) {
        this.idCommentaire = idCommentaire;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.idUtilisateur = idUtilisateur;
        this.idPost = idPost;
    }

    public int getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(int idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "idCommentaire=" + idCommentaire +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", idUtilisateur=" + idUtilisateur +
                ", idPost=" + idPost +
                '}';
    }
}
