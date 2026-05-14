package controllers;

import services.PdfExportService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.File;

public class ScoreResultController {

    @FXML private Label scoreLabel;
    @FXML private Label messageLabel;

    private final PdfExportService pdfExportService = new PdfExportService();

    @FXML
    public void initialize() {
        int score = AppNavigator.getFinalCorrectAnswers();
        int total = AppNavigator.getTotalQuestions();
        int points = AppNavigator.getFinalPoints();
        int totalPoints = AppNavigator.getTotalPoints();
        int bonus = AppNavigator.getBonusPoints();

        scoreLabel.setText(points + " / " + totalPoints + " pts");
        messageLabel.setText((score == total ? "Excellent travail." : "Resultat enregistre.")
                + " Bonnes reponses : " + score + " / " + total
                + ". Bonus rapidite : +" + bonus + " pt(s)."
                + antiCheatMessage());
    }

    @FXML
    void retourQuiz() {
        AppNavigator.load(SessionManager.isQuizManager() ? "QuizManagement.fxml" : "UserQuizSelection.fxml");
    }

    @FXML
    void exportResultsPdf() {
        try {
            File file = pdfExportService.exportResult(AppNavigator.getCurrentQuizResult());
            alert("PDF genere", "Vos resultats sont exportes en PDF.");
            pdfExportService.openExportLocation(file);
        } catch (Exception e) {
            alert("Export PDF impossible", e.getMessage());
        }
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String antiCheatMessage() {
        if (AppNavigator.getCurrentQuizResult() == null) {
            return "";
        }
        int incidents = AppNavigator.getCurrentQuizResult().getAntiCheatIncidentCount();
        if (incidents == 0) {
            return " Anti-triche : aucun incident.";
        }
        String message = " Anti-triche : " + incidents + " incident(s) detecte(s).";
        if (AppNavigator.getCurrentQuizResult().isTerminatedByAntiCheat()) {
            message += " Quiz termine automatiquement.";
        }
        return message;
    }
}
