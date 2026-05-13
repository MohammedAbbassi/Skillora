package controllers;

import entities.Question;
import services.QuestionCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class QuestionsManagementController {

    @FXML private ListView<Question> questionList;

    @FXML private TextField searchQuestionField;
    @FXML private Label searchCountLabel;

    private final QuestionCRUD questionCRUD = new QuestionCRUD();

    @FXML
    public void initialize() {
        if (searchQuestionField != null) {
            searchQuestionField.textProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (questionList != null) {
            questionList.setCellFactory(lv -> new QuestionCardCell());
            questionList.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    Question q = questionList.getSelectionModel().getSelectedItem();
                    if (q != null) {
                        AppNavigator.setSelectedQuizId(q.getQuizId());
                        AppNavigator.setEditQuestionId(q.getId());
                        AppNavigator.load("EditQuestion.fxml");
                    }
                }
            });
        }
        refresh();
    }

    @FXML
    void editQuestion() {
        Question question = questionList.getSelectionModel().getSelectedItem();
        if (question == null) {
            alert("Sélection requise", "Sélectionnez une question.");
            return;
        }
        AppNavigator.setSelectedQuizId(question.getQuizId());
        AppNavigator.setEditQuestionId(question.getId());
        AppNavigator.load("EditQuestion.fxml");
    }

    @FXML
    void deleteQuestion() {
        Question question = questionList.getSelectionModel().getSelectedItem();
        if (question == null) {
            alert("Sélection requise", "Sélectionnez une question.");
            return;
        }
        if (!confirmDelete("Supprimer la question",
                "Voulez-vous vraiment supprimer cette question ?",
                "Question : " + safeText(question.getEnonce())
                        + "\nLes reponses liees a cette question peuvent aussi etre supprimees.")) {
            return;
        }
        try {
            questionCRUD.supprimerQuestion(question.getId());
            refresh();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void addAnswers() {
        Question question = questionList.getSelectionModel().getSelectedItem();
        if (question == null) {
            alert("Sélection requise", "Sélectionnez une question.");
            return;
        }
        AppNavigator.setSelectedQuestion(question.getId(), question.getTypeQuestion());
        AppNavigator.load("AjouterReponse.fxml");
    }

    private void refresh() {
        try {
            String q = searchQuestionField == null ? null : searchQuestionField.getText();
            var items = FXCollections.observableArrayList(questionCRUD.rechercherParEnonce(q));
            questionList.setItems(items);
            if (searchCountLabel != null) {
                searchCountLabel.setText(items.size() + " r\u00e9sultat(s)");
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private class QuestionCardCell extends ListCell<Question> {
        @Override
        protected void updateItem(Question question, boolean empty) {
            super.updateItem(question, empty);
            if (empty || question == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(question.getEnonce() == null ? "" : question.getEnonce());
            title.getStyleClass().add("card-title");
            title.setWrapText(true);

            String metaText = "Type: " + (question.getTypeQuestionValue() == null ? "" : question.getTypeQuestionValue());
            Label meta = new Label(metaText);
            meta.getStyleClass().add("card-meta");

            VBox textBox = new VBox(4, title, meta);
            textBox.getStyleClass().add("card-content");

            Button answers = new Button("Reponses");
            answers.getStyleClass().addAll("secondary-button", "card-action");
            answers.setOnAction(e -> {
                AppNavigator.setSelectedQuestion(question.getId(), question.getTypeQuestion());
                AppNavigator.load("AjouterReponse.fxml");
            });

            Button edit = new Button("Modifier");
            edit.getStyleClass().addAll("secondary-button", "card-action");
            edit.setOnAction(e -> {
                AppNavigator.setSelectedQuizId(question.getQuizId());
                AppNavigator.setEditQuestionId(question.getId());
                AppNavigator.load("EditQuestion.fxml");
            });

            Button delete = new Button("Supprimer");
            delete.getStyleClass().addAll("danger-button", "card-action");
            delete.setOnAction(e -> deleteQuestionFromCard(question));

            VBox actions = new VBox(8, answers, edit, delete);
            actions.getStyleClass().add("card-actions");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox root = new HBox(14, textBox, spacer, actions);
            root.getStyleClass().add("list-card");

            setText(null);
            setGraphic(root);
        }
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteQuestionFromCard(Question question) {
        if (!confirmDelete("Supprimer la question",
                "Voulez-vous vraiment supprimer cette question ?",
                "Question : " + safeText(question.getEnonce())
                        + "\nLes reponses liees a cette question peuvent aussi etre supprimees.")) {
            return;
        }
        try {
            questionCRUD.supprimerQuestion(question.getId());
            refresh();
        } catch (SQLException ex) {
            alert("Erreur SQL", ex.getMessage());
        }
    }

    private boolean confirmDelete(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert.showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent();
    }

    private String safeText(String text) {
        return text == null || text.isBlank() ? "(sans enonce)" : text;
    }
}
