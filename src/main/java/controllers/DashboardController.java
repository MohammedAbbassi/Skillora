package controllers;

import services.QuestionCRUD;
import services.QuizCRUD;
import services.ReponseCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class DashboardController {

    @FXML private Label quizCountLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label answerCountLabel;

    private final QuizCRUD quizCRUD = new QuizCRUD();
    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();

    @FXML
    public void initialize() {
        try {
            quizCountLabel.setText(String.valueOf(quizCRUD.afficherQuiz().size()));
            questionCountLabel.setText(String.valueOf(questionCRUD.afficherQuestions().size()));
            answerCountLabel.setText(String.valueOf(reponseCRUD.afficherReponses().size()));
        } catch (SQLException e) {
            quizCountLabel.setText("-");
            questionCountLabel.setText("-");
            answerCountLabel.setText("-");
        }
    }

    @FXML
    void createQuiz() {
        AppNavigator.setEditQuizId(0);
        AppNavigator.load("AjouterQuiz.fxml");
    }
}
