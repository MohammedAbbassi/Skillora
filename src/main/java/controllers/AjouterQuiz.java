package controllers;

import entities.Quiz;
import entities.Quiz.Matiere;
import services.QuizCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import utils.ValidationUtils;

import java.sql.SQLException;

public class AjouterQuiz {

    @FXML private Label screenTitleLabel;
    @FXML private TextField nomQuizTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<Matiere> matiereCombo;
    @FXML private Button submitButton;

    private final QuizCRUD quizCRUD = new QuizCRUD();
    private int editQuizId;

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("Debutant", "Intermediaire", "Avance", "Expert"));
        matiereCombo.setItems(FXCollections.observableArrayList(Matiere.values()));
        editQuizId = AppNavigator.getEditQuizId();
        if (editQuizId > 0) {
            screenTitleLabel.setText("Modifier le quiz");
            submitButton.setText("Enregistrer");
            loadQuizForEdit();
        }
    }

    @FXML
    void next() {
        Quiz quiz = readForm();
        if (quiz == null) {
            return;
        }

        try {
            if (editQuizId > 0) {
                quiz.setId(editQuizId);
                quizCRUD.modifierQuiz(quiz);
                AppNavigator.setEditQuizId(0);
                AppNavigator.load("QuizManagement.fxml");
            } else {
                quizCRUD.ajouterQuiz(quiz);
                clearForm();
                AppNavigator.setSelectedQuizId(quiz.getId());
                AppNavigator.load("AjouterQuestion.fxml");
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void cancel() {
        AppNavigator.setEditQuizId(0);
        AppNavigator.load("QuizManagement.fxml");
    }

    private void loadQuizForEdit() {
        try {
            Quiz quiz = quizCRUD.getQuizById(editQuizId);
            if (quiz == null) {
                return;
            }
            nomQuizTextField.setText(quiz.getNomQuiz());
            descriptionTextArea.setText(quiz.getDescription());
            niveauCombo.setValue(quiz.getNiveau());
            matiereCombo.setValue(quiz.getMatiere());
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private Quiz readForm() {
        String nomQuiz = ValidationUtils.trimToEmpty(nomQuizTextField.getText());
        String description = ValidationUtils.trimToEmpty(descriptionTextArea.getText());
        String niveau = niveauCombo.getValue();
        Matiere matiere = matiereCombo.getValue();

        if (nomQuiz.isEmpty() || description.isEmpty() || niveau == null || matiere == null) {
            alert("Champs requis", "Veuillez remplir tous les champs du quiz.");
            return null;
        }

        if (nomQuiz.length() < 3) {
            alert("Validation", "Le nom du quiz doit contenir au moins 3 caractères.");
            return null;
        }
        if (description.length() < 3) {
            alert("Validation", "La description doit contenir au moins 3 caractères.");
            return null;
        }

        return new Quiz(nomQuiz, description, niveau, matiere);
    }

    private void clearForm() {
        nomQuizTextField.clear();
        descriptionTextArea.clear();
        niveauCombo.setValue(null);
        matiereCombo.setValue(null);
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
