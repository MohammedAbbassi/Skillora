package controllers;

import entities.Question;
import entities.Reponse;
import entities.Quiz;
import services.OpenAIQuizGeneratorService;
import services.OpenAIQuizGeneratorService.GeneratedAnswer;
import services.OpenAIQuizGeneratorService.GeneratedQuestion;
import services.OpenAIQuizGeneratorService.GeneratedQuiz;
import services.QuestionCRUD;
import services.QuizCRUD;
import services.ReponseCRUD;
import utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AiQuizGeneratorController {

    @FXML private Label quizLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea topicTextArea;
    @FXML private TextField questionCountField;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private Button generateButton;
    @FXML private Button saveButton;
    @FXML private ListView<GeneratedQuestion> previewListView;

    private final OpenAIQuizGeneratorService aiService = new OpenAIQuizGeneratorService();
    private final QuizCRUD quizCRUD = new QuizCRUD();
    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();

    private GeneratedQuiz draft;
    private Quiz selectedQuiz;

    @FXML
    public void initialize() {
        difficultyCombo.setItems(FXCollections.observableArrayList(
                "Auto", "Debutant", "Intermediaire", "Avance", "Expert"
        ));
        difficultyCombo.setValue("Auto");
        questionCountField.setText("5");
        saveButton.setDisable(true);
        previewListView.setPlaceholder(new Label("Les questions generees apparaitront ici avant insertion."));
        previewListView.setCellFactory(listView -> new GeneratedQuestionCell());
        loadQuizTitle();
    }

    @FXML
    void generateWithAi() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            alert("Quiz requis", "Selectionnez ou creez un quiz avant d'utiliser l'IA.");
            return;
        }

        Quiz quiz = getSelectedQuiz();
        if (quiz == null) {
            alert("Quiz requis", "Selectionnez ou creez un quiz avant d'utiliser l'IA.");
            return;
        }

        String topic = buildGenerationTopic(quiz);
        if (topic.length() < 2) {
            alert("Sujet requis", "Le quiz selectionne doit avoir une matiere ou un titre exploitable.");
            return;
        }

        int questionCount;
        try {
            questionCount = Integer.parseInt(questionCountField.getText().trim());
        } catch (NumberFormatException e) {
            alert("Nombre invalide", "Saisissez un nombre de questions valide.");
            return;
        }
        if (questionCount < 1 || questionCount > 15) {
            alert("Nombre invalide", "Generez entre 1 et 15 questions a la fois.");
            return;
        }

        setGenerating(true);
        statusLabel.setText("Generation IA en cours...");

        Task<GeneratedQuiz> task = new Task<>() {
            @Override
            protected GeneratedQuiz call() throws Exception {
                return aiService.generateQuiz(topic, questionCount, getGenerationDifficulty(quiz));
            }
        };

        task.setOnSucceeded(event -> {
            draft = task.getValue();
            normalizeDraft(draft);
            previewListView.setItems(FXCollections.observableArrayList(draft.questions));
            saveButton.setDisable(false);
            if (draft.warning == null || draft.warning.isBlank()) {
                statusLabel.setText(draft.questions.size() + " question(s) generee(s) avec OpenAI. Niveau suggere : "
                        + draft.niveauSuggere);
            } else {
                statusLabel.setText(draft.questions.size() + " question(s) generee(s) en mode local. "
                        + draft.warning);
            }
            setGenerating(false);
        });

        task.setOnFailed(event -> {
            setGenerating(false);
            Throwable error = task.getException();
            statusLabel.setText("Generation echouee.");
            alert("Erreur IA", error == null ? "Erreur inconnue." : error.getMessage());
        });

        Thread thread = new Thread(task, "openai-quiz-generator");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void saveGeneratedQuiz() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0 || draft == null || draft.questions == null || draft.questions.isEmpty()) {
            alert("Aucune generation", "Generez des questions avant d'enregistrer.");
            return;
        }

        try {
            int savedQuestions = 0;
            for (GeneratedQuestion generatedQuestion : draft.questions) {
                Question.TypeQuestion type = "QCM".equalsIgnoreCase(generatedQuestion.type)
                        ? Question.TypeQuestion.QCM
                        : Question.TypeQuestion.QCU;

                Question question = new Question(quizId, generatedQuestion.enonce, type);
                question.setPoint(1);
                questionCRUD.ajouterQuestion(question);

                for (GeneratedAnswer answer : generatedQuestion.reponses) {
                    reponseCRUD.ajouterReponse(new Reponse(question.getId(), answer.texte, answer.correcte));
                }
                savedQuestions++;
            }

            alert("Quiz enrichi", savedQuestions + " question(s) IA ajoutee(s) avec succes.");
            AppNavigator.load("QuestionsManagement.fxml");
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void backToQuestionForm() {
        AppNavigator.load("AjouterQuestion.fxml");
    }

    private void loadQuizTitle() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            quizLabel.setText("Aucun quiz selectionne");
            return;
        }

        try {
            selectedQuiz = quizCRUD.getQuizById(quizId);
            if (selectedQuiz == null) {
                quizLabel.setText("Quiz introuvable");
                return;
            }

            quizLabel.setText(selectedQuiz.getNomQuiz() + " - " + selectedQuiz.getMatiereValue());
            topicTextArea.setText(defaultTopicText(selectedQuiz));
            String quizLevel = normalizeQuizLevel(selectedQuiz.getNiveau());
            if (!quizLevel.isBlank()) {
                difficultyCombo.setValue(quizLevel);
            }
        } catch (SQLException e) {
            quizLabel.setText("Quiz selectionne");
        }
    }

    private Quiz getSelectedQuiz() {
        if (selectedQuiz != null) {
            return selectedQuiz;
        }
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            return null;
        }
        try {
            selectedQuiz = quizCRUD.getQuizById(quizId);
            return selectedQuiz;
        } catch (SQLException e) {
            return null;
        }
    }

    private String buildGenerationTopic(Quiz quiz) {
        StringBuilder topic = new StringBuilder();
        if (quiz.getMatiere() != null) {
            topic.append("Matiere: ").append(quiz.getMatiereValue());
        }
        if (quiz.getNomQuiz() != null && !quiz.getNomQuiz().isBlank()) {
            appendTopicLine(topic, "Theme", quiz.getNomQuiz());
        }
        if (quiz.getDescription() != null && !quiz.getDescription().isBlank()) {
            appendTopicLine(topic, "Description", quiz.getDescription());
        }

        String precision = sanitizeManualTopic(topicTextArea.getText());
        String defaultTopic = sanitizeManualTopic(defaultTopicText(quiz));
        if (!precision.isBlank() && !precision.equalsIgnoreCase(defaultTopic)) {
            appendTopicLine(topic, "Precision", precision);
        }
        return topic.toString().trim();
    }

    private void appendTopicLine(StringBuilder builder, String label, String value) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(label).append(": ").append(value.trim());
    }

    private String defaultTopicText(Quiz quiz) {
        if (quiz == null) {
            return "";
        }
        String matiere = quiz.getMatiereValue();
        String title = ValidationUtils.trimToEmpty(quiz.getNomQuiz());
        if (!matiere.isBlank() && !title.isBlank()) {
            return matiere + " - " + title;
        }
        return !matiere.isBlank() ? matiere : title;
    }

    private String sanitizeManualTopic(String text) {
        String cleaned = ValidationUtils.trimToEmpty(text);
        if (cleaned.isBlank()) {
            return "";
        }

        String lower = cleaned.toLowerCase();
        int topicIndex = lower.lastIndexOf(" sur ");
        if ((lower.contains("genere") || lower.contains("génère") || lower.contains("tu es"))
                && topicIndex >= 0) {
            String extracted = cleaned.substring(topicIndex + 5).trim();
            int end = extracted.indexOf('.');
            if (end > 0) {
                extracted = extracted.substring(0, end).trim();
            }
            return extracted;
        }
        return cleaned;
    }

    private String getGenerationDifficulty(Quiz quiz) {
        String selectedDifficulty = difficultyCombo.getValue();
        if (selectedDifficulty != null && !selectedDifficulty.isBlank() && !"Auto".equalsIgnoreCase(selectedDifficulty)) {
            return selectedDifficulty;
        }

        String quizLevel = normalizeQuizLevel(quiz.getNiveau());
        return quizLevel.isBlank() ? "Intermediaire" : quizLevel;
    }

    private String normalizeQuizLevel(String level) {
        String normalized = ValidationUtils.trimToEmpty(level).toLowerCase();
        if (normalized.contains("debut") || normalized.contains("début")) {
            return "Debutant";
        }
        if (normalized.contains("inter")) {
            return "Intermediaire";
        }
        if (normalized.contains("avance") || normalized.contains("avanc")) {
            return "Avance";
        }
        if (normalized.contains("expert")) {
            return "Expert";
        }
        return "";
    }

    private void normalizeDraft(GeneratedQuiz generatedQuiz) {
        if (generatedQuiz.niveauSuggere == null || generatedQuiz.niveauSuggere.isBlank()) {
            generatedQuiz.niveauSuggere = "Auto";
        }

        for (GeneratedQuestion question : generatedQuiz.questions) {
            if (!"QCM".equalsIgnoreCase(question.type)) {
                question.type = "QCU";
            } else {
                question.type = "QCM";
            }

            ensureMinimumAnswers(question);

            int correctCount = 0;
            for (GeneratedAnswer answer : question.reponses) {
                if (answer.texte == null || answer.texte.isBlank()) {
                    answer.texte = "Reponse";
                }
                if (answer.correcte) {
                    correctCount++;
                }
            }
            if (correctCount == 0) {
                question.reponses.get(0).correcte = true;
                correctCount = 1;
            }
            if ("QCU".equals(question.type) && correctCount > 1) {
                keepOnlyFirstCorrect(question.reponses);
            }
            if ("QCM".equals(question.type) && correctCount > 2) {
                keepFirstCorrectAnswers(question.reponses, 2);
            }
        }
    }

    private void ensureMinimumAnswers(GeneratedQuestion question) {
        if (question.reponses == null) {
            question.reponses = new ArrayList<>();
        } else if (!(question.reponses instanceof ArrayList)) {
            question.reponses = new ArrayList<>(question.reponses);
        }

        while (question.reponses.size() < ReponseCRUD.MIN_REPONSES_PAR_QUESTION) {
            question.reponses.add(defaultAnswer(question.reponses.isEmpty()));
        }
    }

    private GeneratedAnswer defaultAnswer(boolean correct) {
        GeneratedAnswer answer = new GeneratedAnswer();
        answer.texte = correct ? "Reponse correcte" : "Option";
        answer.correcte = correct;
        return answer;
    }

    private void keepOnlyFirstCorrect(List<GeneratedAnswer> answers) {
        keepFirstCorrectAnswers(answers, 1);
    }

    private void keepFirstCorrectAnswers(List<GeneratedAnswer> answers, int maxCorrect) {
        int kept = 0;
        for (GeneratedAnswer answer : answers) {
            if (answer.correcte) {
                kept++;
                if (kept > maxCorrect) {
                    answer.correcte = false;
                }
            }
        }
    }

    private void setGenerating(boolean generating) {
        generateButton.setDisable(generating);
        saveButton.setDisable(generating || draft == null);
        topicTextArea.setDisable(generating);
        questionCountField.setDisable(generating);
        difficultyCombo.setDisable(generating);
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class GeneratedQuestionCell extends ListCell<GeneratedQuestion> {
        @Override
        protected void updateItem(GeneratedQuestion question, boolean empty) {
            super.updateItem(question, empty);
            if (empty || question == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(question.enonce == null ? "" : question.enonce);
            title.setWrapText(true);
            title.getStyleClass().add("ai-preview-question");

            Label meta = new Label(question.type + "  -  " + question.reponses.size() + " reponses");
            meta.getStyleClass().add("ai-preview-meta");

            VBox answersBox = new VBox(6);
            for (GeneratedAnswer answer : question.reponses) {
                Label answerLabel = new Label((answer.correcte ? "[Correcte] " : "[Option] ") + answer.texte);
                answerLabel.setWrapText(true);
                answerLabel.getStyleClass().add(answer.correcte ? "ai-answer-correct" : "ai-answer-option");
                answersBox.getChildren().add(answerLabel);
            }

            VBox text = new VBox(8, title, meta, answersBox);
            text.getStyleClass().add("ai-preview-card");

            HBox root = new HBox(text);
            root.getStyleClass().add("ai-preview-shell");

            setText(null);
            setGraphic(root);
        }
    }
}
