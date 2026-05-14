package entities;

import java.util.ArrayList;
import java.util.List;

public class QuizResultDetail {

    private String questionText;
    private String questionType;
    private List<String> selectedAnswers = new ArrayList<>();
    private List<String> correctAnswers = new ArrayList<>();
    private boolean correct;

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<String> selectedAnswers) {
        this.selectedAnswers = selectedAnswers == null ? new ArrayList<>() : selectedAnswers;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers == null ? new ArrayList<>() : correctAnswers;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
