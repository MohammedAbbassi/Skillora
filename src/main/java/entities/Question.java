package entities;

public class Question {

    public enum TypeQuestion {
        QCU, QCM
    }

    private int id;
    private int quizId;
    private String enonce;
    private TypeQuestion typeQuestion;
    private int point = 1;
    private String imagePath;

    public Question() {
    }

    public Question(int quizId, String enonce, TypeQuestion typeQuestion) {
        this.quizId = quizId;
        this.enonce = enonce;
        this.typeQuestion = typeQuestion;
        this.point = 1;
    }

    public Question(int id, int quizId, String enonce, TypeQuestion typeQuestion) {
        this.id = id;
        this.quizId = quizId;
        this.enonce = enonce;
        this.typeQuestion = typeQuestion;
        this.point = 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getEnonce() {
        return enonce;
    }

    public void setEnonce(String enonce) {
        this.enonce = enonce;
    }

    public TypeQuestion getTypeQuestion() {
        return typeQuestion;
    }

    public void setTypeQuestion(TypeQuestion typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    public String getTypeQuestionValue() {
        return typeQuestion == null ? "" : typeQuestion.name();
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return enonce;
    }
}
