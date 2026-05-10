package controllers;

import entities.Question;
import entities.QuizResult;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public final class AppNavigator {

    private static StackPane contentPane;
    private static int selectedQuizId;
    private static int selectedQuestionId;
    private static Question.TypeQuestion selectedQuestionType;
    private static int editQuizId;
    private static int editQuestionId;
    private static int editReponseId;
    // Quiz results: keep both "correct answers count" and "points" since questions can have weights.
    private static int finalCorrectAnswers;
    private static int totalQuestions;
    private static int finalPoints;
    private static int totalPoints;
    private static int bonusPoints;
    private static boolean darkMode;
    private static boolean quizInProgress;
    private static QuizResult currentQuizResult;

    private AppNavigator() {
    }

    public static void setContentPane(StackPane pane) {
        contentPane = pane;
    }

    public static void load(String fxml) {
        if (contentPane == null) {
            return;
        }

        try {
            Parent view = FXMLLoader.load(AppNavigator.class.getResource("/" + fxml));
            view.setOpacity(0);
            contentPane.getChildren().setAll(view);
            FadeTransition fade = new FadeTransition(Duration.millis(180), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load view: " + fxml, e);
        }
    }

    public static int getSelectedQuizId() {
        return selectedQuizId;
    }

    public static void setSelectedQuizId(int selectedQuizId) {
        AppNavigator.selectedQuizId = selectedQuizId;
    }

    public static int getSelectedQuestionId() {
        return selectedQuestionId;
    }

    public static Question.TypeQuestion getSelectedQuestionType() {
        return selectedQuestionType;
    }

    public static void setSelectedQuestion(int questionId, Question.TypeQuestion typeQuestion) {
        AppNavigator.selectedQuestionId = questionId;
        AppNavigator.selectedQuestionType = typeQuestion;
    }

    public static int getEditQuizId() {
        return editQuizId;
    }

    public static void setEditQuizId(int editQuizId) {
        AppNavigator.editQuizId = editQuizId;
    }

    public static int getEditQuestionId() {
        return editQuestionId;
    }

    public static void setEditQuestionId(int editQuestionId) {
        AppNavigator.editQuestionId = editQuestionId;
    }

    public static int getEditReponseId() {
        return editReponseId;
    }

    public static void setEditReponseId(int editReponseId) {
        AppNavigator.editReponseId = editReponseId;
    }

    public static int getTotalQuestions() {
        return totalQuestions;
    }

    public static int getFinalCorrectAnswers() {
        return finalCorrectAnswers;
    }

    public static int getFinalPoints() {
        return finalPoints;
    }

    public static int getTotalPoints() {
        return totalPoints;
    }

    public static int getBonusPoints() {
        return bonusPoints;
    }

    public static void setScoreResult(int finalCorrectAnswers, int totalQuestions, int finalPoints, int totalPoints) {
        setScoreResult(finalCorrectAnswers, totalQuestions, finalPoints, totalPoints, 0);
    }

    public static void setScoreResult(int finalCorrectAnswers, int totalQuestions, int finalPoints, int totalPoints, int bonusPoints) {
        AppNavigator.finalCorrectAnswers = finalCorrectAnswers;
        AppNavigator.totalQuestions = totalQuestions;
        AppNavigator.finalPoints = finalPoints;
        AppNavigator.totalPoints = totalPoints;
        AppNavigator.bonusPoints = bonusPoints;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        AppNavigator.darkMode = darkMode;
    }

    public static boolean isQuizInProgress() {
        return quizInProgress;
    }

    public static void setQuizInProgress(boolean quizInProgress) {
        AppNavigator.quizInProgress = quizInProgress;
    }

    public static QuizResult getCurrentQuizResult() {
        return currentQuizResult;
    }

    public static void setCurrentQuizResult(QuizResult currentQuizResult) {
        AppNavigator.currentQuizResult = currentQuizResult;
    }
}
