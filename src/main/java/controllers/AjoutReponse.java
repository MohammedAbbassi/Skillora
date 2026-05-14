package controllers;

import entities.Question;
import entities.Question.TypeQuestion;
import entities.Reponse;
import services.QuestionCRUD;
import services.ReponseCRUD;
import utils.ValidationUtils;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AjoutReponse {

    @FXML private Label typeQuestionLabel;
    @FXML private Label questionLabel;
    @FXML private Label workflowMessageLabel;
    @FXML private TextField pointTextField;
    @FXML private TextField texteTextField;
    @FXML private CheckBox correcteCheck;
    @FXML private Button ajouterReponseButton;
    @FXML private Button terminerButton;
    @FXML private VBox completionPanel;
    @FXML private ListView<String> reponsesListView;
    @FXML private ListView<String> questionsTimelineListView;

    private final ReponseCRUD reponseCRUD = new ReponseCRUD();
    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private boolean questionTerminee;

    @FXML
    public void initialize() {
        TypeQuestion typeQuestion = AppNavigator.getSelectedQuestionType();
        typeQuestionLabel.setText(typeQuestion == null ? "Type non defini" : "Type : " + typeQuestion.name());
        reponsesListView.setPlaceholder(new Label("Les reponses ajoutees apparaitront ici."));
        questionsTimelineListView.setPlaceholder(new Label("Les questions du quiz apparaitront ici."));
        completionPanel.setVisible(false);
        completionPanel.setManaged(false);

        chargerQuestion();
        refreshReponses();
        refreshTimeline();
    }

    public void setQuestion(int questionId, TypeQuestion typeQuestion) {
        AppNavigator.setSelectedQuestion(questionId, typeQuestion);
    }

    @FXML
    void ajouterReponse() {
        if (questionTerminee) {
            return;
        }

        int questionId = AppNavigator.getSelectedQuestionId();
        TypeQuestion typeQuestion = AppNavigator.getSelectedQuestionType();
        if (questionId == 0 || typeQuestion == null) {
            alert("Question requise", "Selectionnez une question avant d'ajouter des reponses.");
            return;
        }

        String texte = ValidationUtils.trimToEmpty(texteTextField.getText());
        if (texte.isEmpty()) {
            alert("Champ requis", "Veuillez saisir le texte de la reponse.");
            return;
        }
        if (texte.length() < 3) {
            alert("Validation", "Le texte de la reponse doit contenir au moins 3 caracteres.");
            return;
        }

        try {
            questionCRUD.modifierPointQuestion(questionId, 1);

            if (typeQuestion == TypeQuestion.QCU && correcteCheck.isSelected()
                    && !reponseCRUD.getCorrectAnswersByQuestion(questionId).isEmpty()) {
                alert("Regle QCU", "Une question QCU ne peut avoir qu'une seule bonne reponse.");
                return;
            }
            if (typeQuestion == TypeQuestion.QCM && correcteCheck.isSelected()
                    && reponseCRUD.getCorrectAnswersByQuestion(questionId).size() >= 2) {
                alert("Regle QCM", "Une question QCM ne peut avoir que 2 bonnes reponses au maximum.");
                return;
            }

            reponseCRUD.ajouterReponse(new Reponse(questionId, texte, correcteCheck.isSelected()));
            texteTextField.clear();
            correcteCheck.setSelected(false);
            refreshReponses();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void terminer() {
        if (!enregistrerReponseEnCoursSiNecessaire() || !questionCouranteValide()) {
            return;
        }

        questionTerminee = true;
        texteTextField.setDisable(true);
        correcteCheck.setDisable(true);
        ajouterReponseButton.setDisable(true);
        terminerButton.setDisable(true);

        workflowMessageLabel.setText("Question enregistree. Ajoutez une autre question ou finalisez le quiz.");
        refreshTimeline();
        showCompletionPanel();
    }

    private boolean enregistrerReponseEnCoursSiNecessaire() {
        if (ValidationUtils.trimToEmpty(texteTextField.getText()).isEmpty()) {
            return true;
        }

        int previousCount = reponsesListView.getItems() == null ? 0 : reponsesListView.getItems().size();
        ajouterReponse();
        int newCount = reponsesListView.getItems() == null ? 0 : reponsesListView.getItems().size();
        return newCount > previousCount;
    }

    @FXML
    void ajouterNouvelleQuestion() {
        AppNavigator.setSelectedQuestion(0, null);
        AppNavigator.load("AjouterQuestion.fxml");
    }

    @FXML
    void finaliserQuiz() {
        AppNavigator.setSelectedQuestion(0, null);
        AppNavigator.load("QuizManagement.fxml");
    }

    private boolean questionCouranteValide() {
        int questionId = AppNavigator.getSelectedQuestionId();
        TypeQuestion typeQuestion = AppNavigator.getSelectedQuestionType();
        if (questionId == 0 || typeQuestion == null) {
            alert("Question requise", "Selectionnez une question avant de terminer.");
            return false;
        }

        try {
            List<Reponse> reponses = reponseCRUD.afficherReponsesParQuestion(questionId);
            if (reponses.size() < ReponseCRUD.MIN_REPONSES_PAR_QUESTION) {
                alert("Reponses requises", "Ajoutez au moins "
                        + ReponseCRUD.MIN_REPONSES_PAR_QUESTION
                        + " reponses avant de terminer cette question.");
                return false;
            }

            long correctCount = reponses.stream().filter(Reponse::isCorrecte).count();
            if (correctCount == 0) {
                alert("Reponse correcte requise", "Marquez au moins une reponse comme correcte.");
                return false;
            }
            if (typeQuestion == TypeQuestion.QCU && correctCount != 1) {
                alert("Regle QCU", "Une question QCU doit avoir exactement une bonne reponse.");
                return false;
            }
            if (typeQuestion == TypeQuestion.QCM && correctCount > 2) {
                alert("Regle QCM", "Une question QCM ne peut avoir que 2 bonnes reponses au maximum.");
                return false;
            }

            return true;
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
            return false;
        }
    }

    private void showCompletionPanel() {
        completionPanel.setOpacity(0);
        completionPanel.setVisible(true);
        completionPanel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(220), completionPanel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void chargerQuestion() {
        int questionId = AppNavigator.getSelectedQuestionId();
        pointTextField.setText("1");
        pointTextField.setEditable(false);

        if (questionId == 0) {
            questionLabel.setText("Aucune question selectionnee");
            return;
        }

        try {
            Question question = questionCRUD.getQuestionById(questionId);
            questionLabel.setText(question == null ? "Question introuvable" : question.getEnonce());
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void refreshReponses() {
        int questionId = AppNavigator.getSelectedQuestionId();
        if (questionId == 0) {
            return;
        }
        try {
            var reponses = FXCollections.observableArrayList(
                    reponseCRUD.afficherReponsesParQuestion(questionId)
                            .stream()
                            .map(reponse -> (reponse.isCorrecte() ? "[Correcte] " : "[Option] ") + reponse.getTexte())
                            .collect(Collectors.toList())
            );
            reponsesListView.setItems(reponses);
            if (!reponses.isEmpty()) {
                reponsesListView.scrollTo(reponses.size() - 1);
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void refreshTimeline() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            return;
        }

        try {
            List<Question> questions = questionCRUD.afficherParQuiz(quizId);
            var timelineItems = FXCollections.<String>observableArrayList();
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                timelineItems.add("Question " + (i + 1) + "  -  " + question.getTypeQuestionValue()
                        + "\n" + question.getEnonce());
            }
            questionsTimelineListView.setItems(timelineItems);
            if (!timelineItems.isEmpty()) {
                questionsTimelineListView.scrollTo(timelineItems.size() - 1);
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
