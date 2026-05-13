package entities;

import java.sql.Timestamp;

public class Notification {
    private long idNotification;
    private String type; // MENTION, LIKE, COMMENT, REPLY
    private String message;
    private boolean lu;
    private Timestamp dateCreation;
    private long idUtilisateur; // Receiver
    private Long idDeclencheur; // Trigger user (can be null)
    private Integer idPost;
    private Integer idCommentaire;

    public Notification() {
    }

    public Notification(String type, String message, long idUtilisateur, Long idDeclencheur) {
        this.type = type;
        this.message = message;
        this.lu = false;
        this.idUtilisateur = idUtilisateur;
        this.idDeclencheur = idDeclencheur;
    }

    public Notification(String type, String message, long idUtilisateur, Long idDeclencheur, 
                       Integer idPost, Integer idCommentaire) {
        this.type = type;
        this.message = message;
        this.lu = false;
        this.idUtilisateur = idUtilisateur;
        this.idDeclencheur = idDeclencheur;
        this.idPost = idPost;
        this.idCommentaire = idCommentaire;
    }

    // Getters and Setters
    public long getIdNotification() { return idNotification; }
    public void setIdNotification(long idNotification) { this.idNotification = idNotification; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(long idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Long getIdDeclencheur() { return idDeclencheur; }
    public void setIdDeclencheur(Long idDeclencheur) { this.idDeclencheur = idDeclencheur; }

    public Integer getIdPost() { return idPost; }
    public void setIdPost(Integer idPost) { this.idPost = idPost; }

    public Integer getIdCommentaire() { return idCommentaire; }
    public void setIdCommentaire(Integer idCommentaire) { this.idCommentaire = idCommentaire; }

    @Override
    public String toString() {
        return "Notification{" +
                "idNotification=" + idNotification +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", lu=" + lu +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
