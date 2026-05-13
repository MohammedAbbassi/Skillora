package controllers;

import entities.Reponse;
import services.ReponseCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import utils.ValidationUtils;

import java.sql.SQLException;

public class EditReponseController {

    @FXML private TextField texteTextField;
    @FXML private CheckBox correcteCheck;

    private final ReponseCRUD reponseCRUD = new ReponseCRUD();
    private final services.QuestionCRUD questionCRUD = new services.QuestionCRUD();
    private Reponse currentReponse;

    @FXML
    public void initialize() {
        int editReponseId = AppNavigator.getEditReponseId();
        if (editReponseId <= 0) {
            cancel();
            return;
        }

        try {
            currentReponse = reponseCRUD.getReponseById(editReponseId);
            if (currentReponse == null) {
                alert("Erreur", "Réponse introuvable.");
                cancel();
                return;
            }
            texteTextField.setText(currentReponse.getTexte());
            correcteCheck.setSelected(currentReponse.isCorrecte());
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void save() {
        if (currentReponse == null) return;

        String texte = ValidationUtils.trimToEmpty(texteTextField.getText());
        if (texte.isEmpty()) {
            alert("Champ requis", "Veuillez saisir le texte de la réponse.");
            return;
        }

        if (texte.length() < 3) {
            alert("Validation", "Le texte de la rÃ©ponse doit contenir au moins 3 caractÃ¨res.");
            return;
        }

        boolean isCorrect = correcteCheck.isSelected();

        try {
            entities.Question question = questionCRUD.getQuestionById(currentReponse.getQuestionId());
            if (question != null && isCorrect) {
                if (question.getTypeQuestion() == entities.Question.TypeQuestion.QCU) {
                    java.util.List<Reponse> correctAnswers = reponseCRUD.getCorrectAnswersByQuestion(question.getId());
                    boolean otherCorrectExists = correctAnswers.stream().anyMatch(r -> r.getId() != currentReponse.getId());
                    if (otherCorrectExists) {
                        alert("Règle QCU", "Une question QCU ne peut avoir qu'une seule bonne réponse.");
                        return;
                    }
                } else if (question.getTypeQuestion() == entities.Question.TypeQuestion.QCM) {
                    java.util.List<Reponse> correctAnswers = reponseCRUD.getCorrectAnswersByQuestion(question.getId());
                    long otherCorrectCount = correctAnswers.stream().filter(r -> r.getId() != currentReponse.getId()).count();
                    if (otherCorrectCount >= 2) {
                        alert("Règle QCM", "Une question QCM ne peut avoir que 2 bonnes réponses au maximum.");
                        return;
                    }
                }
            }

            currentReponse.setTexte(texte);
            currentReponse.setCorrecte(isCorrect);

            reponseCRUD.modifierReponse(currentReponse);
            AppNavigator.setEditReponseId(0);
            AppNavigator.load("AnswersManagement.fxml");
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void cancel() {
        AppNavigator.setEditReponseId(0);
        AppNavigator.load("AnswersManagement.fxml");
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
