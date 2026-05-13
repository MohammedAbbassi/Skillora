package entities;

import java.time.LocalDateTime;

public class Quiz {

    public enum Matiere {
        JAVA, HTML, CSS, JAVASCRIPT, PHP, PYTHON, SQL
    }

    private int id;
    private String nomQuiz;
    private String description;
    private String niveau;
    private Matiere matiere;
    private LocalDateTime dateAjout;

    public Quiz() {
    }

    public Quiz(String nomQuiz, String description, String niveau, Matiere matiere) {
        this.nomQuiz = nomQuiz;
        this.description = description;
        this.niveau = niveau;
        this.matiere = matiere;
    }

    public Quiz(int id, String nomQuiz, String description, String niveau, Matiere matiere) {
        this.id = id;
        this.nomQuiz = nomQuiz;
        this.description = description;
        this.niveau = niveau;
        this.matiere = matiere;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomQuiz() {
        return nomQuiz;
    }

    public void setNomQuiz(String nomQuiz) {
        this.nomQuiz = nomQuiz;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public String getMatiereValue() {
        return matiere == null ? "" : matiere.name();
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    @Override
    public String toString() {
        return nomQuiz;
    }
}
