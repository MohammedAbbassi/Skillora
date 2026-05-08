package entities;

public class Question {

    public enum TypeQuestion {
        QCU, QCM
    }

    private int id;
    private int quizId;
    private String libelle;
    private String enonce;
    private TypeQuestion typeQuestion;
    private int point;
    private String imagePath;
    private String niveau;
    private boolean estActive;
    private int idUtilisateur;

    public Question() {}

    public Question(int quizId, String enonce, TypeQuestion typeQuestion) {
        this.quizId = quizId;
        this.enonce = enonce;
        this.typeQuestion = typeQuestion;
        this.point = 1;
        this.estActive = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public TypeQuestion getTypeQuestion() { return typeQuestion; }
    public void setTypeQuestion(TypeQuestion typeQuestion) { this.typeQuestion = typeQuestion; }
    public String getTypeQuestionValue() { return typeQuestion == null ? "" : typeQuestion.name(); }
    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public boolean isEstActive() { return estActive; }
    public void setEstActive(boolean estActive) { this.estActive = estActive; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    @Override
    public String toString() { return enonce; }
}
