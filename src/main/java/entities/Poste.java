package entities;

import java.sql.Timestamp;

public class Poste {
    private int idPost;
    private String titre;
    private String contenu;
    private String image;
    private Timestamp dateCreation;
    private int idUtilisateur;
    /** null = original post | non-null = this is a repost of originalPostId */
    private Integer originalPostId;

    public Poste() {
    }

    public Poste(String titre, String contenu, String image, int idUtilisateur) {
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.idUtilisateur = idUtilisateur;
    }

    public Poste(int idPost, String titre, String contenu, String image, Timestamp dateCreation, int idUtilisateur) {
        this.idPost = idPost;
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.dateCreation = dateCreation;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Integer getOriginalPostId() { return originalPostId; }
    public void setOriginalPostId(Integer originalPostId) { this.originalPostId = originalPostId; }

    /** Returns true if this post is a repost of another post */
    public boolean isRepost() { return originalPostId != null; }

    @Override
    public String toString() {
        return "Poste{" +
                "idPost=" + idPost +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", image='" + image + '\'' +
                ", dateCreation=" + dateCreation +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}
