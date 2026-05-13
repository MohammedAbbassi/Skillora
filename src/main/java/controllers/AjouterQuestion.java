package controllers;

import entities.Question;
import entities.Question.TypeQuestion;
import services.QuestionCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import utils.ValidationUtils;

public class AjouterQuestion {

    @FXML private javafx.scene.control.Label screenTitleLabel;
    @FXML private TextArea enonceTextArea;
    @FXML private ComboBox<TypeQuestion> typeQuestionCombo;
    @FXML private javafx.scene.control.Button removeImageBtn;
    @FXML private ImageView questionImageView;
    @FXML private javafx.scene.control.Label imagePathLabel;

    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private int editQuestionId;
    private String currentImagePath = null;
    private static final String UPLOAD_DIR = "uploads/questions/";

    @FXML
    public void initialize() {
        // Only QCU/QCM are supported in the UI.
        typeQuestionCombo.setItems(FXCollections.observableArrayList(TypeQuestion.QCU, TypeQuestion.QCM));
        editQuestionId = AppNavigator.getEditQuestionId();
        if (editQuestionId > 0) {
            screenTitleLabel.setText("Modifier une question");
            chargerQuestion();
        }
    }

    public void setQuizId(int quizId) {
        AppNavigator.setSelectedQuizId(quizId);
    }

    @FXML
    void next() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            alert("Quiz requis", "SÃ©lectionnez ou crÃ©ez un quiz avant d'ajouter une question.");
            return;
        }

        String enonce = ValidationUtils.trimToEmpty(enonceTextArea.getText());
        TypeQuestion typeQuestion = typeQuestionCombo.getValue();
        if (enonce.isEmpty() || typeQuestion == null) {
            alert("Champs requis", "Veuillez saisir l'Ã©noncÃ© et le type de question.");
            return;
        }
        if (enonce.length() < 3) {
            alert("Validation", "L'Ã©noncÃ© doit contenir au moins 3 caractÃ¨res.");
            return;
        }

        try {
            Question question = new Question(quizId, enonce, typeQuestion);
            question.setImagePath(currentImagePath);

            if (editQuestionId > 0) {
                services.ReponseCRUD reponseCRUD = new services.ReponseCRUD();

                if (typeQuestion == TypeQuestion.QCU) {
                    if (reponseCRUD.getCorrectAnswersByQuestion(editQuestionId).size() > 1) {
                        alert("RÃ¨gle QCU", "Cette question a plusieurs rÃ©ponses correctes. Veuillez en modifier certaines avant de passer en QCU.");
                        return;
                    }
                } else if (typeQuestion == TypeQuestion.QCM) {
                    if (reponseCRUD.getCorrectAnswersByQuestion(editQuestionId).size() > 2) {
                        alert("RÃ¨gle QCM", "Cette question a plus de 2 rÃ©ponses correctes. Veuillez en modifier certaines avant de passer en QCM.");
                        return;
                    }
                } else {
                    alert("Type invalide", "Type de question non supporte. Veuillez choisir QCU ou QCM.");
                    return;
                }

                question.setId(editQuestionId);
                question.setPoint(1);
                questionCRUD.modifierQuestion(question);
                AppNavigator.setEditQuestionId(0);
                AppNavigator.load("QuestionsManagement.fxml");
                return;
            }

            questionCRUD.ajouterQuestion(question);
            enonceTextArea.clear();
            typeQuestionCombo.setValue(null);
            AppNavigator.setSelectedQuestion(question.getId(), typeQuestion);
            AppNavigator.load("AjouterReponse.fxml");
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void generateWithAi() {
        int quizId = AppNavigator.getSelectedQuizId();
        if (quizId == 0) {
            alert("Quiz requis", "Selectionnez ou creez un quiz avant d'utiliser l'IA.");
            return;
        }
        AppNavigator.load("AiQuizGenerator.fxml");
    }

    @FXML
    void cancel() {
        boolean editing = AppNavigator.getEditQuestionId() > 0;
        AppNavigator.setEditQuestionId(0);
        AppNavigator.load(editing ? "QuestionsManagement.fxml" : "QuizManagement.fxml");
    }

    private void chargerQuestion() {
        try {
            Question question = questionCRUD.getQuestionById(editQuestionId);
            if (question == null) {
                return;
            }
            AppNavigator.setSelectedQuizId(question.getQuizId());
            enonceTextArea.setText(question.getEnonce());
            typeQuestionCombo.setValue(question.getTypeQuestion());

            if (question.getImagePath() != null && !question.getImagePath().isEmpty()) {
                currentImagePath = question.getImagePath();
                File file = new File(currentImagePath);
                if (file.exists()) {
                    Image image = new Image("file:" + currentImagePath);
                    questionImageView.setImage(image);
                    imagePathLabel.setText(file.getName());
                    removeImageBtn.setVisible(true);
                    removeImageBtn.setManaged(true);
                }
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

    @FXML
    void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour la question");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(screenTitleLabel.getScene().getWindow());
        if (selectedFile != null) {
            try {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = Paths.get(UPLOAD_DIR, fileName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                currentImagePath = destination.toString();
                Image image = new Image("file:" + currentImagePath);
                questionImageView.setImage(image);
                imagePathLabel.setText(selectedFile.getName());
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
            } catch (IOException e) {
                alert("Erreur", "Impossible de copier l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    void removeImage() {
        currentImagePath = null;
        questionImageView.setImage(null);
        imagePathLabel.setText("");
        removeImageBtn.setVisible(false);
        removeImageBtn.setManaged(false);
    }
}
