package entities;

import java.sql.Timestamp;

public class Commentaire {
    private int idCommentaire;
    private String contenu;
    private Timestamp dateCreation;
    private int idUtilisateur;
    private int idPost;
    private Integer parentId;

    public Commentaire() {}

    public Commentaire(String contenu, int idUtilisateur, int idPost) {
        this.contenu = contenu;
        this.idUtilisateur = idUtilisateur;
        this.idPost = idPost;
    }

    public int getIdCommentaire() { return idCommentaire; }
    public void setIdCommentaire(int id) { this.idCommentaire = id; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp ts) { this.dateCreation = ts; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int id) { this.idUtilisateur = id; }
    public int getIdPost() { return idPost; }
    public void setIdPost(int id) { this.idPost = id; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
}
