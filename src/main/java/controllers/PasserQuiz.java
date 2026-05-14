package controllers;

import entities.Question;
import entities.Question.TypeQuestion;
import entities.Quiz;
import entities.QuizResult;
import entities.QuizResultDetail;
import entities.Reponse;
import services.AntiCheatApi;
import services.AntiCheatService.Incident;
import services.QuestionCRUD;
import services.QuizCRUD;
import services.QuizResultHistoryService;
import services.ReponseCRUD;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PasserQuiz {

    @FXML private VBox quizRoot;
    @FXML private Label progressionLabel;
    @FXML private Label scoreLabel;
    @FXML private Label antiCheatLabel;
    @FXML private ProgressBar globalTimerProgress;
    @FXML private ProgressBar questionTimerProgress;
    @FXML private Label bonusLabel;
    @FXML private Label questionLabel;
    @FXML private VBox optionsBox;
    @FXML private Button suivantButton;
    @FXML private javafx.scene.image.ImageView questionImageView;

    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final QuizCRUD quizCRUD = new QuizCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();
    private final QuizResultHistoryService resultHistoryService = new QuizResultHistoryService();
    private final AntiCheatApi antiCheatApi = new AntiCheatApi();
    private final ToggleGroup qcuGroup = new ToggleGroup();
    private final List<Question> questions = new ArrayList<>();
    private final List<QuizResultDetail> resultDetails = new ArrayList<>();
    private Quiz currentQuiz;
    private int currentIndex;
    private int earnedPoints;
    private int correctAnswers;
    private int bonusPoints;
    private int globalRemainingSeconds;
    private int questionRemainingSeconds;
    private boolean adminPreview;
    private boolean quizFinished;
    private boolean terminatedByAntiCheat;
    private boolean antiCheatInstalled;
    private boolean antiCheatAlertOpen;
    private long lastTimerTickMillis;
    private Timeline timerTimeline;

    private static final int QUESTION_TIME_SECONDS = 15;
    private static final int RAPID_BONUS_THRESHOLD_SECONDS = 5;
    private static final int RAPID_BONUS_POINTS = 1;
    private static final int TIMER_GAP_ALERT_SECONDS = 4;

    @FXML
    public void initialize() {
        adminPreview = SessionManager.isQuizManager();
        setQuizId(AppNavigator.getSelectedQuizId());
    }

    public void setQuizId(int quizId) {
        try {
            currentQuiz = quizCRUD.getQuizById(quizId);
            questions.clear();
            questions.addAll(questionCRUD.afficherParQuiz(quizId));
            resultDetails.clear();
            AppNavigator.setCurrentQuizResult(null);
            currentIndex = 0;
            earnedPoints = 0;
            correctAnswers = 0;
            bonusPoints = 0;
            quizFinished = false;
            stopTimer();

            // Admin preview: read-only consultation, no scoring UI.
            if (adminPreview) {
                AppNavigator.setQuizInProgress(false);
                scoreLabel.setVisible(false);
                scoreLabel.setManaged(false);
                setTimerPanelVisible(false);
            } else {
                AppNavigator.setQuizInProgress(!questions.isEmpty());
                antiCheatApi.startSession(
                        System.getProperty("user.name", SessionManager.getRole().name()),
                        currentQuiz == null ? "Quiz" : currentQuiz.getNomQuiz()
                );
                scoreLabel.setVisible(true);
                scoreLabel.setManaged(true);
                setTimerPanelVisible(true);
                globalRemainingSeconds = questions.size() * QUESTION_TIME_SECONDS;
                questionRemainingSeconds = QUESTION_TIME_SECONDS;
                updateTimerDisplay();
                updateAntiCheatDisplay();
                Platform.runLater(this::installAntiCheatGuards);
            }

            afficherQuestionCourante();
            if (!adminPreview && !questions.isEmpty()) {
                startTimer();
            }
        } catch (SQLException e) {
            afficherAlerte("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void suivant(ActionEvent event) {
        if (questions.isEmpty()) {
            return;
        }

        try {
            if (adminPreview) {
                if (currentIndex >= questions.size() - 1) {
                    AppNavigator.load("Dashboard.fxml");
                } else {
                    currentIndex++;
                    afficherQuestionCourante();
                }
                return;
            }

            validerQuestionCourante(true);
        } catch (SQLException e) {
            afficherAlerte("Erreur SQL", e.getMessage());
        }
    }

    private void startTimer() {
        stopTimer();
        lastTimerTickMillis = System.currentTimeMillis();
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> tickTimer()));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    private void tickTimer() {
        if (adminPreview || quizFinished || questions.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (!antiCheatAlertOpen && lastTimerTickMillis > 0) {
            long elapsedSeconds = (now - lastTimerTickMillis) / 1000;
            if (elapsedSeconds >= TIMER_GAP_ALERT_SECONDS) {
                handleAntiCheatIncident(antiCheatApi.recordTimerSuspension(elapsedSeconds, currentQuestionNumber()));
            }
        }
        lastTimerTickMillis = now;

        globalRemainingSeconds--;
        questionRemainingSeconds--;
        updateTimerDisplay();

        if (questionRemainingSeconds <= 0) {
            try {
                validerQuestionCourante(false);
            } catch (SQLException e) {
                stopTimer();
                afficherAlerte("Erreur SQL", e.getMessage());
            }
            return;
        }

        if (globalRemainingSeconds <= 0) {
            terminerQuiz();
        }
    }

    private void validerQuestionCourante(boolean requireSelection) throws SQLException {
        if (questions.isEmpty() || currentIndex >= questions.size()) {
            return;
        }

        if (requireSelection && !hasAtLeastOneSelection()) {
            afficherAlerte("Reponse requise", "Veuillez selectionner au moins une reponse avant de passer a la question suivante.");
            return;
        }

        QuizResultDetail detail = buildResultDetail();
        resultDetails.add(detail);

        if (detail.isCorrect()) {
            correctAnswers++;
            earnedPoints += Math.max(1, questions.get(currentIndex).getPoint());
            if (questionRemainingSeconds >= RAPID_BONUS_THRESHOLD_SECONDS) {
                bonusPoints += RAPID_BONUS_POINTS;
            }
        }

        currentIndex++;
        if (currentIndex < questions.size()) {
            questionRemainingSeconds = QUESTION_TIME_SECONDS;
            updateTimerDisplay();
        }
        afficherQuestionCourante();
    }

    private boolean hasAtLeastOneSelection() {
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) {
            return false;
        }

        Question question = questions.get(currentIndex);
        if (question.getTypeQuestion() == TypeQuestion.QCU) {
            return qcuGroup.getSelectedToggle() != null;
        }

        if (question.getTypeQuestion() == TypeQuestion.QCM) {
            for (javafx.scene.Node node : optionsBox.getChildren()) {
                if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private void afficherQuestionCourante() throws SQLException {
        optionsBox.getChildren().clear();

        if (questions.isEmpty()) {
            progressionLabel.setText("Aucune question");
            questionLabel.setText("Ce quiz ne contient pas encore de questions.");
            if (!adminPreview) {
                scoreLabel.setText("Score : 0");
            }
            suivantButton.setDisable(true);
            return;
        }

        if (currentIndex >= questions.size()) {
            terminerQuiz();
            return;
        }

        Question question = questions.get(currentIndex);
        if (adminPreview) {
            progressionLabel.setText("Previsualisation: question " + (currentIndex + 1) + " / " + questions.size());
            if (currentIndex >= questions.size() - 1) {
                suivantButton.setText("Retour au tableau de bord");
            } else {
                suivantButton.setText("Suivant");
            }
        } else {
            progressionLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());
            scoreLabel.setText("Score : " + (earnedPoints + bonusPoints) + " pts");
            updateTimerDisplay();
            suivantButton.setText("Suivant");
        }
        questionLabel.setText(question.getEnonce());
        suivantButton.setDisable(false);

        questionImageView.setImage(null);
        questionImageView.setVisible(false);
        questionImageView.setManaged(false);

        if (question.getImagePath() != null && !question.getImagePath().isEmpty()) {
            File file = new File(question.getImagePath());
            if (file.exists()) {
                Image image = new Image("file:" + question.getImagePath());
                questionImageView.setImage(image);
                questionImageView.setVisible(true);
                questionImageView.setManaged(true);
            }
        }

        List<Reponse> reponses = reponseCRUD.afficherReponsesParQuestion(question.getId());
        if (question.getTypeQuestion() == TypeQuestion.QCU) {
            qcuGroup.getToggles().clear();
            for (Reponse reponse : reponses) {
                RadioButton radio = new RadioButton(reponse.getTexte());
                radio.setUserData(reponse);
                radio.setToggleGroup(qcuGroup);

                if (adminPreview) {
                    radio.setDisable(true);
                    if (reponse.isCorrecte()) {
                        radio.setSelected(true);
                        markAsCorrect(radio);
                    }
                }

                optionsBox.getChildren().add(radio);
            }
        } else if (question.getTypeQuestion() == TypeQuestion.QCM) {
            for (Reponse reponse : reponses) {
                CheckBox checkBox = new CheckBox(reponse.getTexte());
                checkBox.setUserData(reponse);

                if (adminPreview) {
                    checkBox.setDisable(true);
                    if (reponse.isCorrecte()) {
                        checkBox.setSelected(true);
                        markAsCorrect(checkBox);
                    }
                } else {
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            long selectedCount = optionsBox.getChildren().stream()
                                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                                    .count();
                            if (selectedCount > 2) {
                                Platform.runLater(() -> {
                                    checkBox.setSelected(false);
                                    afficherAlerte("Limite atteinte", "Vous ne pouvez choisir que 2 rÃ©ponses au maximum.");
                                });
                            }
                        }
                    });
                }

                optionsBox.getChildren().add(checkBox);
            }
        }
    }

    private void markAsCorrect(javafx.scene.control.Labeled control) {
        // Visual cue for correct answers in admin preview.
        control.setStyle("-fx-text-fill: #1b5e20; -fx-font-weight: bold;");
    }

    private boolean reponseCorrecte() throws SQLException {
        Question question = questions.get(currentIndex);

        if (question.getTypeQuestion() == TypeQuestion.QCU) {
            Toggle selected = qcuGroup.getSelectedToggle();
            return selected != null && ((Reponse) selected.getUserData()).isCorrecte();
        }

        if (question.getTypeQuestion() == TypeQuestion.QCM) {
            Set<Integer> selectedIds = new HashSet<>();
            for (javafx.scene.Node node : optionsBox.getChildren()) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    selectedIds.add(((Reponse) checkBox.getUserData()).getId());
                }
            }

            Set<Integer> correctIds = new HashSet<>();
            for (Reponse reponse : reponseCRUD.getCorrectAnswersByQuestion(question.getId())) {
                correctIds.add(reponse.getId());
            }
            return !correctIds.isEmpty() && selectedIds.equals(correctIds);
        }

        return false;
    }

    private QuizResultDetail buildResultDetail() throws SQLException {
        Question question = questions.get(currentIndex);
        List<Reponse> selectedReponses = selectedReponses();
        List<Reponse> correctReponses = reponseCRUD.getCorrectAnswersByQuestion(question.getId());

        Set<Integer> selectedIds = new HashSet<>();
        for (Reponse reponse : selectedReponses) {
            selectedIds.add(reponse.getId());
        }

        Set<Integer> correctIds = new HashSet<>();
        for (Reponse reponse : correctReponses) {
            correctIds.add(reponse.getId());
        }

        QuizResultDetail detail = new QuizResultDetail();
        detail.setQuestionText(question.getEnonce());
        detail.setQuestionType(question.getTypeQuestionValue());
        detail.setSelectedAnswers(reponseTexts(selectedReponses));
        detail.setCorrectAnswers(reponseTexts(correctReponses));
        detail.setCorrect(!correctIds.isEmpty() && selectedIds.equals(correctIds));
        return detail;
    }

    private List<Reponse> selectedReponses() {
        List<Reponse> selected = new ArrayList<>();
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) {
            return selected;
        }

        Question question = questions.get(currentIndex);
        if (question.getTypeQuestion() == TypeQuestion.QCU) {
            Toggle selectedToggle = qcuGroup.getSelectedToggle();
            if (selectedToggle != null) {
                selected.add((Reponse) selectedToggle.getUserData());
            }
            return selected;
        }

        if (question.getTypeQuestion() == TypeQuestion.QCM) {
            for (javafx.scene.Node node : optionsBox.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) node;
                    if (checkBox.isSelected()) {
                        selected.add((Reponse) checkBox.getUserData());
                    }
                }
            }
        }
        return selected;
    }

    private List<String> reponseTexts(List<Reponse> reponses) {
        List<String> texts = new ArrayList<>();
        for (Reponse reponse : reponses) {
            texts.add(reponse.getTexte());
        }
        return texts;
    }

    private int totalPoints() {
        int total = 0;
        for (Question question : questions) {
            total += Math.max(1, question.getPoint());
        }
        return total;
    }

    private void terminerQuiz() {
        if (quizFinished) {
            return;
        }

        quizFinished = true;
        AppNavigator.setQuizInProgress(false);
        stopTimer();

        if (adminPreview) {
            AppNavigator.load("Dashboard.fxml");
            return;
        }

        int totalQ = questions.size();
        int maxBonus = totalQ * RAPID_BONUS_POINTS;
        int totalPts = totalPoints() + maxBonus;
        AppNavigator.setScoreResult(correctAnswers, totalQ, earnedPoints + bonusPoints, totalPts, bonusPoints);
        QuizResult result = buildQuizResult(totalQ, totalPts);
        saveResultHistory(result);
        AppNavigator.setCurrentQuizResult(result);
        if (totalQ > 0 && correctAnswers == totalQ && antiCheatApi.getIncidentCount() == 0) {
            AppNavigator.load("Celebration.fxml");
        } else {
            AppNavigator.load("ScoreResult.fxml");
        }
    }

    private QuizResult buildQuizResult(int totalQ, int totalPts) {
        QuizResult result = new QuizResult();
        result.setQuizId(currentQuiz == null ? 0 : currentQuiz.getId());
        result.setUserName(System.getProperty("user.name", SessionManager.getRole().name()));
        result.setQuizTitle(currentQuiz == null ? "Quiz" : currentQuiz.getNomQuiz());
        result.setFinalPoints(earnedPoints + bonusPoints);
        result.setTotalPoints(totalPts);
        result.setCorrectAnswers(correctAnswers);
        result.setTotalQuestions(totalQ);
        result.setBonusPoints(bonusPoints);
        result.setAntiCheatIncidentCount(antiCheatApi.getIncidentCount());
        result.setAntiCheatCriticalCount(antiCheatApi.getCriticalIncidentCount());
        result.setAntiCheatSummary(antiCheatApi.getSummary());
        result.setTerminatedByAntiCheat(terminatedByAntiCheat);
        try {
            result.setAntiCheatReportPath(antiCheatApi.writeReport());
        } catch (IOException e) {
            result.setAntiCheatReportPath("Rapport anti-triche non genere: " + e.getMessage());
        }
        result.setCompletedAt(LocalDateTime.now());
        result.setDetails(new ArrayList<>(resultDetails));
        return result;
    }

    private void saveResultHistory(QuizResult result) {
        try {
            resultHistoryService.saveResult(result);
        } catch (SQLException e) {
            afficherAlerte("Historique", "Resultat non enregistre dans l'historique: " + e.getMessage());
        }
    }

    private void updateTimerDisplay() {
        if (globalTimerProgress != null) {
            int totalGlobalSeconds = Math.max(1, questions.size() * QUESTION_TIME_SECONDS);
            globalTimerProgress.setProgress(progressRatio(globalRemainingSeconds, totalGlobalSeconds));
        }
        if (questionTimerProgress != null) {
            questionTimerProgress.setProgress(progressRatio(questionRemainingSeconds, QUESTION_TIME_SECONDS));
        }
        if (bonusLabel != null) {
            bonusLabel.setText("Bonus : +" + bonusPoints);
        }
        updateAntiCheatDisplay();
    }

    private double progressRatio(int remainingSeconds, int totalSeconds) {
        return Math.max(0, Math.min(1, remainingSeconds / (double) Math.max(1, totalSeconds)));
    }

    private void setTimerPanelVisible(boolean visible) {
        if (globalTimerProgress != null) {
            globalTimerProgress.setVisible(visible);
            globalTimerProgress.setManaged(visible);
        }
        if (questionTimerProgress != null) {
            questionTimerProgress.setVisible(visible);
            questionTimerProgress.setManaged(visible);
        }
        if (bonusLabel != null) {
            bonusLabel.setVisible(visible);
            bonusLabel.setManaged(visible);
        }
        if (antiCheatLabel != null) {
            antiCheatLabel.setVisible(visible);
            antiCheatLabel.setManaged(visible);
        }
    }

    private void installAntiCheatGuards() {
        if (adminPreview || questions.isEmpty() || antiCheatInstalled || quizRoot == null) {
            return;
        }

        Scene scene = quizRoot.getScene();
        if (scene == null) {
            Platform.runLater(this::installAntiCheatGuards);
            return;
        }

        antiCheatInstalled = true;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleAntiCheatKey);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleAntiCheatMouse);
        scene.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordContextMenu(currentQuestionNumber()));
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED, event ->
                handleAntiCheatIncident(antiCheatApi.recordMouseExited(currentQuestionNumber())));

        Window window = scene.getWindow();
        if (window == null) {
            return;
        }
        window.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                handleAntiCheatIncident(antiCheatApi.recordFocusLost(currentQuestionNumber()));
            }
        });
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            stage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
                if (isIconified) {
                    handleAntiCheatIncident(antiCheatApi.recordWindowMinimized(currentQuestionNumber()));
                }
            });
        }
    }

    private void handleAntiCheatKey(KeyEvent event) {
        if (adminPreview || quizFinished || questions.isEmpty() || antiCheatAlertOpen) {
            return;
        }

        KeyCode code = event.getCode();
        boolean shortcut = event.isControlDown() || event.isMetaDown();
        if (code == KeyCode.PRINTSCREEN) {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordScreenshotAttempt(currentQuestionNumber()));
            return;
        }
        if (event.isAltDown() && code == KeyCode.TAB) {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordAltTabAttempt(currentQuestionNumber()));
            return;
        }
        if (shortcut && (code == KeyCode.C || code == KeyCode.V || code == KeyCode.X
                || code == KeyCode.A || code == KeyCode.P || code == KeyCode.S
                || code == KeyCode.N || code == KeyCode.T || code == KeyCode.W
                || code == KeyCode.L || code == KeyCode.F)) {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordForbiddenShortcut(code.getName(), currentQuestionNumber()));
            return;
        }
        if (event.isControlDown() && event.isShiftDown() && code == KeyCode.I) {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordDevToolsAttempt(currentQuestionNumber()));
        }
    }

    private void handleAntiCheatMouse(MouseEvent event) {
        if (adminPreview || quizFinished || questions.isEmpty() || antiCheatAlertOpen) {
            return;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            event.consume();
            handleAntiCheatIncident(antiCheatApi.recordRightClick(currentQuestionNumber()));
        }
    }

    private void handleAntiCheatIncident(Incident incident) {
        if (adminPreview || quizFinished || questions.isEmpty() || antiCheatAlertOpen) {
            return;
        }

        updateAntiCheatDisplay();

        antiCheatAlertOpen = true;
        try {
            if (antiCheatApi.shouldFinishQuiz()) {
                terminatedByAntiCheat = true;
                afficherAlerte("Anti-triche",
                        "Trop d'incidents anti-triche ont ete detectes. Le quiz va etre termine.\n\n"
                                + incident.getMessage());
                terminerQuiz();
                return;
            }

            afficherAlerte("Alerte anti-triche",
                    incident.getMessage()
                            + "\n\nIncident(s): " + antiCheatApi.getIncidentCount()
                            + " | Critique(s): " + antiCheatApi.getCriticalIncidentCount());
        } finally {
            antiCheatAlertOpen = false;
            lastTimerTickMillis = System.currentTimeMillis();
        }
    }

    private void updateAntiCheatDisplay() {
        if (antiCheatLabel != null) {
            antiCheatLabel.setText(antiCheatApi.getStatusText());
        }
    }

    private int currentQuestionNumber() {
        return currentIndex + 1;
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
