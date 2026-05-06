package entities;

import java.sql.Timestamp;

public class Jaime {
    private int idPost;
    private int idUtilisateur;
    private Timestamp dateJaime;

    public Jaime() {}
    public Jaime(int idPost, int idUtilisateur) {
        this.idPost = idPost;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdPost() { return idPost; }
    public void setIdPost(int id) { this.idPost = id; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int id) { this.idUtilisateur = id; }
    public Timestamp getDateJaime() { return dateJaime; }
    public void setDateJaime(Timestamp ts) { this.dateJaime = ts; }
}
