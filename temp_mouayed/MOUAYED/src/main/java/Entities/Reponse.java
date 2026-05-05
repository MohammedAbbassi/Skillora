package Entities;

import java.time.LocalDateTime;

public class Reponse {

    private int id;
    private String contenu;
    private boolean estCorrecte;
    private int questionId;
    private String commentaire;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean active;
    private String auteur;
    private String source;
    private String typeReponse;

    public Reponse() {
    }

    public Reponse(String contenu, boolean estCorrecte, int questionId, String commentaire,
                   LocalDateTime dateCreation, LocalDateTime dateModification,
                   boolean active, String auteur, String source, String typeReponse) {
        this.contenu = contenu;
        this.estCorrecte = estCorrecte;
        this.questionId = questionId;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.active = active;
        this.auteur = auteur;
        this.source = source;
        this.typeReponse = typeReponse;
    }

    public Reponse(int id, String contenu, boolean estCorrecte, int questionId, String commentaire,
                   LocalDateTime dateCreation, LocalDateTime dateModification,
                   boolean active, String auteur, String source, String typeReponse) {
        this.id = id;
        this.contenu = contenu;
        this.estCorrecte = estCorrecte;
        this.questionId = questionId;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.active = active;
        this.auteur = auteur;
        this.source = source;
        this.typeReponse = typeReponse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public boolean isEstCorrecte() {
        return estCorrecte;
    }

    public void setEstCorrecte(boolean estCorrecte) {
        this.estCorrecte = estCorrecte;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTypeReponse() {
        return typeReponse;
    }

    public void setTypeReponse(String typeReponse) {
        this.typeReponse = typeReponse;
    }

    // Compatibilité avec ton ancien code
    public boolean isEst_correcte() {
        return estCorrecte;
    }

    public void setEst_correcte(boolean estCorrecte) {
        this.estCorrecte = estCorrecte;
    }

    public int getId_question() {
        return questionId;
    }

    public void setId_question(int questionId) {
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", estCorrecte=" + estCorrecte +
                ", questionId=" + questionId +
                ", commentaire='" + commentaire + '\'' +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                ", active=" + active +
                ", auteur='" + auteur + '\'' +
                ", source='" + source + '\'' +
                ", typeReponse='" + typeReponse + '\'' +
                '}';
    }
}