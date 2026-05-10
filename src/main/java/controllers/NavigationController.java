package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.util.List;

public class NavigationController {

    @FXML private BorderPane appRoot;
    @FXML private StackPane contentPane;
    @FXML private Button dashboardButton;
    @FXML private Button quizButton;
    @FXML private Button questionsButton;
    @FXML private Button answersButton;
    @FXML private Button historyButton;
    @FXML private Button roleToggleButton;
    @FXML private Button themeToggleButton;

    @FXML
    public void initialize() {
        AppNavigator.setContentPane(contentPane);
        applyTheme();
        updateNavigationForRole();
    }

    private void updateNavigationForRole() {
        if (SessionManager.isAdmin()) {
            configureAdminNavigation();
            showDashboard();
            if (roleToggleButton != null) roleToggleButton.setText("Passer en Utilisateur");
        } else {
            configureUserNavigation();
            showUserQuizSelection();
            if (roleToggleButton != null) roleToggleButton.setText("Passer en Admin");
        }
    }

    @FXML
    void toggleRole() {
        if (blockNavigationDuringQuiz()) {
            return;
        }
        if (SessionManager.isAdmin()) {
            SessionManager.setRole(SessionManager.Role.USER);
        } else {
            SessionManager.setRole(SessionManager.Role.ADMIN);
        }
        updateNavigationForRole();
    }

    @FXML
    void toggleTheme() {
        AppNavigator.setDarkMode(!AppNavigator.isDarkMode());
        applyTheme();
    }

    @FXML
    void showDashboard() {
        if (!ensureAdmin()) {
            return;
        }
        activate(dashboardButton);
        AppNavigator.load("Dashboard.fxml");
    }

    @FXML
    void showQuizManagement() {
        if (!ensureAdmin()) {
            return;
        }
        activate(quizButton);
        AppNavigator.load("QuizManagement.fxml");
    }

    @FXML
    void showQuestions() {
        if (!ensureAdmin()) {
            return;
        }
        activate(questionsButton);
        AppNavigator.load("QuestionsManagement.fxml");
    }

    @FXML
    void showAnswers() {
        if (!ensureAdmin()) {
            return;
        }
        activate(answersButton);
        AppNavigator.load("AnswersManagement.fxml");
    }

    @FXML
    void showHistory() {
        if (!ensureAdmin()) {
            return;
        }
        activate(historyButton);
        AppNavigator.load("QuizHistory.fxml");
    }

    private void showUserQuizSelection() {
        if (blockNavigationDuringQuiz()) {
            return;
        }
        activate(quizButton);
        AppNavigator.load("UserQuizSelection.fxml");
    }

    private boolean blockNavigationDuringQuiz() {
        if (!AppNavigator.isQuizInProgress()) {
            return false;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Quiz en cours");
        alert.setHeaderText(null);
        alert.setContentText("Vous ne pouvez pas quitter le quiz pendant une tentative. Terminez le quiz avant de changer d'ecran.");
        alert.showAndWait();
        return true;
    }

    private boolean ensureAdmin() {
        if (SessionManager.isAdmin()) {
            return true;
        }
        showUserQuizSelection();
        return false;
    }

    private void configureAdminNavigation() {
        dashboardButton.setVisible(true);
        dashboardButton.setManaged(true);
        questionsButton.setVisible(true);
        questionsButton.setManaged(true);
        answersButton.setVisible(true);
        answersButton.setManaged(true);
        historyButton.setVisible(true);
        historyButton.setManaged(true);
        quizButton.setText("Quiz");
    }

    private void configureUserNavigation() {
        dashboardButton.setVisible(false);
        dashboardButton.setManaged(false);
        questionsButton.setVisible(false);
        questionsButton.setManaged(false);
        answersButton.setVisible(false);
        answersButton.setManaged(false);
        historyButton.setVisible(false);
        historyButton.setManaged(false);
        quizButton.setText("Choisir un quiz");
    }

    private void activate(Button activeButton) {
        List<Button> buttons = List.of(dashboardButton, quizButton, questionsButton, answersButton, historyButton);
        for (Button button : buttons) {
            button.getStyleClass().remove("nav-active");
        }
        if (!activeButton.getStyleClass().contains("nav-active")) {
            activeButton.getStyleClass().add("nav-active");
        }
    }

    private void applyTheme() {
        if (appRoot == null) {
            return;
        }

        boolean darkMode = AppNavigator.isDarkMode();
        appRoot.getStyleClass().remove("dark-mode");
        if (darkMode) {
            appRoot.getStyleClass().add("dark-mode");
        }
        if (themeToggleButton != null) {
            themeToggleButton.setText(darkMode ? "Mode clair" : "Mode sombre");
        }
    }
}
