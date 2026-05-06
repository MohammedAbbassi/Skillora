package entities;

import java.sql.Timestamp;

public class Poste {
    private int idPost;
    private String titre;
    private String contenu;
    private String image;
    private Timestamp dateCreation;
    private int idUtilisateur;

    public Poste() {}

    public Poste(String titre, String contenu, String image, int idUtilisateur) {
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdPost() { return idPost; }
    public void setIdPost(int idPost) { this.idPost = idPost; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
}
