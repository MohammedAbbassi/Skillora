package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Chapitre;
import entities.QuizQuestion;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import services.TriviaApiService;
import java.util.concurrent.CompletableFuture;

public class QuizController {

    @FXML private Label progressLabel;
    @FXML private ProgressBar quizProgressBar;
    @FXML private VBox questionArea;
    @FXML private Label questionTitle;
    @FXML private VBox optionsContainer;
    @FXML private Button btnNext;
    @FXML private Button btnSubmit;
    @FXML private VBox resultBox;
    @FXML private Label scoreLabel;
    @FXML private Label percentageLabel;

    private List<QuizQuestion> questions = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private int currentIndex = 0;
    private ToggleGroup currentGroup;
    private TriviaApiService triviaApiService = new TriviaApiService();

    private Chapitre currentChapter;
    private final Gson gson = new Gson();

    public void setChapter(Chapitre ch) {
        this.currentChapter = ch;
        loadLocalQuestions();
    }

    private void loadLocalQuestions() {
        if (currentChapter != null && currentChapter.getQuizJson() != null && !currentChapter.getQuizJson().isEmpty()) {
            try {
                Type listType = new TypeToken<ArrayList<QuizQuestion>>(){}.getType();
                this.questions = gson.fromJson(currentChapter.getQuizJson(), listType);
                
                if (this.questions != null && !this.questions.isEmpty()) {
                    this.userAnswers = new ArrayList<>(questions.size());
                    for (int i = 0; i < questions.size(); i++) userAnswers.add(null);
                    showCurrentQuestion();
                } else {
                    showError("Le quiz est vide.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors du chargement du quiz : format JSON invalide.");
            }
        } else {
            showError("Aucun quiz n'est disponible pour ce chapitre.");
        }
    }

    private void showError(String message) {
        questionTitle.setText(message);
        questionTitle.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        btnNext.setDisable(true);
        btnSubmit.setDisable(true);
    }

    private void showCurrentQuestion() {
        if (questions.isEmpty()) return;

        QuizQuestion q = questions.get(currentIndex);
        
        // Update Progress
        progressLabel.setText("Question " + (currentIndex + 1) + " sur " + questions.size());
        quizProgressBar.setProgress((double) (currentIndex + 1) / questions.size());

        // Update Question Title
        questionTitle.setText((currentIndex + 1) + ". " + q.getQuestion());
        questionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Update Options
        optionsContainer.getChildren().clear();
        currentGroup = new ToggleGroup();

        // On mélange les options pour l'affichage si ce n'est pas déjà fait
        List<String> options = new ArrayList<>(q.getOptions());
        Collections.shuffle(options);

        for (String option : options) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(currentGroup);
            rb.setStyle("-fx-font-size: 15px; -fx-text-fill: #334155; -fx-cursor: hand; -fx-padding: 10 20; -fx-background-color: #f1f5f9; -fx-background-radius: 8;");
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.setMinHeight(45);
            optionsContainer.getChildren().add(rb);
        }

        // Manage Buttons
        boolean isLast = (currentIndex == questions.size() - 1);
        btnNext.setVisible(!isLast);
        btnNext.setManaged(!isLast);
        btnSubmit.setVisible(isLast);
        btnSubmit.setManaged(isLast);
    }

    @FXML
    void onNext(ActionEvent event) {
        if (currentGroup.getSelectedToggle() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez choisir une réponse avant de continuer.");
            alert.showAndWait();
            return;
        }

        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showCurrentQuestion();
        }
    }

    private void saveCurrentAnswer() {
        RadioButton selected = (RadioButton) currentGroup.getSelectedToggle();
        if (selected != null) {
            userAnswers.set(currentIndex, selected.getText());
        }
    }

    @FXML
    void onValidate(ActionEvent event) {
        if (currentGroup.getSelectedToggle() == null) {
            onNext(null); // Réutilise la validation
            return;
        }

        saveCurrentAnswer();
        
        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers.get(i) != null && userAnswers.get(i).trim().equalsIgnoreCase(questions.get(i).getCorrectAnswer().trim())) {
                correctCount++;
            }
        }

        double percentage = ((double) correctCount / questions.size()) * 100;
        boolean isSuccess = percentage >= 60; // Seuil de 60%

        scoreLabel.setText("Score : " + correctCount + " / " + questions.size());
        percentageLabel.setText("Réussite : " + String.format("%.1f", percentage) + "%");

        if (isSuccess) {
            resultBox.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 40; -fx-background-radius: 15; -fx-border-color: #bbf7d0; -fx-border-width: 2;");
            scoreLabel.setStyle("-fx-font-size: 45px; -fx-font-weight: 900; -fx-text-fill: #16a34a;");
        } else {
            resultBox.setStyle("-fx-background-color: #fef2f2; -fx-padding: 40; -fx-background-radius: 15; -fx-border-color: #fecaca; -fx-border-width: 2;");
            scoreLabel.setStyle("-fx-font-size: 45px; -fx-font-weight: 900; -fx-text-fill: #dc2626;");
            percentageLabel.setText(percentageLabel.getText() + " (Échec : minimum 60% requis)");
        }

        questionArea.setVisible(false);
        questionArea.setManaged(false);
        btnSubmit.setVisible(false);
        btnSubmit.setManaged(false);
        btnNext.setVisible(false);
        btnNext.setManaged(false);
        
        resultBox.setVisible(true);
        resultBox.setManaged(true);
    }


    @FXML
    void onFetchApiQuiz(ActionEvent event) {
        questionTitle.setText("Chargement des questions depuis l'API...");
        optionsContainer.getChildren().clear();
        
        triviaApiService.fetchQuizzes().thenAccept(newQuestions -> {
            javafx.application.Platform.runLater(() -> {
                if (newQuestions != null && !newQuestions.isEmpty()) {
                    this.questions = newQuestions;
                    this.currentIndex = 0;
                    this.userAnswers = new ArrayList<>(questions.size());
                    for (int i = 0; i < questions.size(); i++) userAnswers.add(null);
                    
                    resultBox.setVisible(false);
                    resultBox.setManaged(false);
                    questionArea.setVisible(true);
                    questionArea.setManaged(true);
                    btnNext.setVisible(true);
                    btnNext.setManaged(true);
                    btnSubmit.setVisible(false);
                    btnSubmit.setManaged(false);
                    
                    showCurrentQuestion();
                } else {
                    showError("Erreur lors du chargement des questions.");
                }
            });
        });
    }

    @FXML
    void onQuit(ActionEvent event) {
        if (currentChapter != null) {
            ChapitreDetailController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-detail.fxml");
            if (ctrl != null) {
                ctrl.setChapter(currentChapter, null); // Retour direct au chapitre
            }
        } else {
            MainLayoutController.getInstance().loadView("/ui/chapitre-list.fxml");
        }
    }
}
