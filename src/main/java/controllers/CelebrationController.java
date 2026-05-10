package controllers;

import services.PdfExportService;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class CelebrationController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Pane confettiPane;
    @FXML private Label scoreLabel;

    private Random random = new Random();
    private final PdfExportService pdfExportService = new PdfExportService();

    @FXML
    public void initialize() {
        int score = AppNavigator.getFinalCorrectAnswers();
        int total = AppNavigator.getTotalQuestions();
        int points = AppNavigator.getFinalPoints();
        int totalPoints = AppNavigator.getTotalPoints();
        int bonus = AppNavigator.getBonusPoints();
        if (scoreLabel != null) {
            scoreLabel.setText("Score : " + points + " / " + totalPoints
                    + " pts | Bonnes reponses : " + score + " / " + total
                    + " | Bonus : +" + bonus
                    + " | Anti-triche : " + antiCheatIncidents() + " incident(s)");
        }

        // Fade in animation for the background
        FadeTransition ftBg = new FadeTransition(Duration.millis(1500), rootPane);
        ftBg.setFromValue(0.0);
        ftBg.setToValue(1.0);
        ftBg.play();

        // Zoom animation for the text and buttons
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), contentBox);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1.0);
        st.setToY(1.0);
        
        // Fade in for the text and buttons
        FadeTransition ftContent = new FadeTransition(Duration.millis(1000), contentBox);
        ftContent.setFromValue(0.0);
        ftContent.setToValue(1.0);

        st.play();
        ftContent.play();

        // Start dynamic confetti animation
        startConfettiAnimation();
    }

    private void startConfettiAnimation() {
        // Spawn a new confetti/star every 100 milliseconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> spawnConfetti()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void spawnConfetti() {
        boolean isStar = random.nextBoolean();
        Shape shape;

        if (isStar) {
            shape = createStar();
        } else {
            shape = new Circle(random.nextDouble() * 5 + 3);
        }

        // Golden colors palette
        Color[] colors = {Color.GOLD, Color.web("#FFD700"), Color.web("#FFA500"), Color.web("#FF8C00")};
        shape.setFill(colors[random.nextInt(colors.length)]);

        // Start position (bottom of screen, random X across the width)
        double startX = random.nextDouble() * rootPane.getPrefWidth();
        if (startX == 0) startX = 500; // fallback if prefWidth is 0
        double startY = rootPane.getPrefHeight() + 50; // start slightly below the visible area
        if (startY <= 50) startY = 800;

        shape.setLayoutX(startX);
        shape.setLayoutY(startY);
        
        confettiPane.getChildren().add(shape);

        // Animation: move upwards and slightly horizontally
        double durationMs = 2500 + random.nextDouble() * 2500; // Between 2.5s and 5s
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), shape);
        tt.setByY(-1000 - random.nextDouble() * 300); // Move up
        tt.setByX((random.nextDouble() - 0.5) * 400); // Drift left or right
        
        // Cleanup after animation finishes
        tt.setOnFinished(e -> confettiPane.getChildren().remove(shape)); 

        // Optional fade out at the end so they don't just disappear abruptly
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), shape);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        tt.play();
        ft.play();
    }

    private Polygon createStar() {
        Polygon star = new Polygon();
        double radius = random.nextDouble() * 10 + 5;
        double innerRadius = radius / 2.5;
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i;
            double r = (i % 2 == 0) ? radius : innerRadius;
            star.getPoints().addAll(Math.cos(angle) * r, Math.sin(angle) * r);
        }
        return star;
    }

    @FXML
    void rejouer() {
        AppNavigator.load("PasserQuiz.fxml");
    }

    @FXML
    void accueil() {
        AppNavigator.load(SessionManager.isAdmin() ? "QuizManagement.fxml" : "UserQuizSelection.fxml");
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

    private int antiCheatIncidents() {
        return AppNavigator.getCurrentQuizResult() == null
                ? 0
                : AppNavigator.getCurrentQuizResult().getAntiCheatIncidentCount();
    }
}
