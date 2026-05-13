package controllers;

import entities.Reponse;
import services.ReponseCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class AnswersManagementController {

    @FXML private ListView<Reponse> answerList;

    @FXML private TextField searchAnswerField;
    @FXML private Label searchCountLabel;

    private final ReponseCRUD reponseCRUD = new ReponseCRUD();

    @FXML
    public void initialize() {
        if (searchAnswerField != null) {
            searchAnswerField.textProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (answerList != null) {
            answerList.setCellFactory(lv -> new AnswerCardCell());
            answerList.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    Reponse r = answerList.getSelectionModel().getSelectedItem();
                    if (r != null) {
                        AppNavigator.setEditReponseId(r.getId());
                        AppNavigator.load("EditReponse.fxml");
                    }
                }
            });
        }
        refresh();
    }

    @FXML
    void editAnswer() {
        Reponse reponse = answerList.getSelectionModel().getSelectedItem();
        if (reponse == null) {
            alert("Sélection requise", "Sélectionnez une réponse.");
            return;
        }
        AppNavigator.setEditReponseId(reponse.getId());
        AppNavigator.load("EditReponse.fxml");
    }

    @FXML
    void deleteAnswer() {
        Reponse reponse = answerList.getSelectionModel().getSelectedItem();
        if (reponse == null) {
            alert("Sélection requise", "Sélectionnez une réponse.");
            return;
        }
        deleteAnswer(reponse);
    }

    private void deleteAnswer(Reponse reponse) {
        try {
            if (reponseCRUD.compterReponsesParQuestion(reponse.getQuestionId())
                    <= ReponseCRUD.MIN_REPONSES_PAR_QUESTION) {
                alert("Suppression impossible", "Une question doit garder au minimum "
                        + ReponseCRUD.MIN_REPONSES_PAR_QUESTION + " reponses.");
                return;
            }

            if (!confirmDelete("Supprimer la reponse",
                    "Voulez-vous vraiment supprimer cette reponse ?",
                    "Reponse : " + safeText(reponse.getTexte()))) {
                return;
            }

            reponseCRUD.supprimerReponse(reponse.getId());
            refresh();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void refresh() {
        try {
            String q = searchAnswerField == null ? null : searchAnswerField.getText();
            var items = FXCollections.observableArrayList(reponseCRUD.rechercherParTexte(q));
            answerList.setItems(items);
            if (searchCountLabel != null) {
                searchCountLabel.setText(items.size() + " r\u00e9sultat(s)");
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private class AnswerCardCell extends ListCell<Reponse> {
        @Override
        protected void updateItem(Reponse reponse, boolean empty) {
            super.updateItem(reponse, empty);
            if (empty || reponse == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(reponse.getTexte() == null ? "" : reponse.getTexte());
            title.getStyleClass().add("card-title");
            title.setWrapText(true);

            Label pill = new Label(reponse.isCorrecte() ? "Correcte" : "Incorrecte");
            pill.getStyleClass().add(reponse.isCorrecte() ? "card-pill-ok" : "card-pill-bad");

            HBox metaRow = new HBox(8, pill);
            metaRow.getStyleClass().add("card-meta-row");

            VBox textBox = new VBox(6, title, metaRow);
            textBox.getStyleClass().add("card-content");

            Button edit = new Button("Modifier");
            edit.getStyleClass().addAll("secondary-button", "card-action");
            edit.setOnAction(e -> {
                AppNavigator.setEditReponseId(reponse.getId());
                AppNavigator.load("EditReponse.fxml");
            });

            Button delete = new Button("Supprimer");
            delete.getStyleClass().addAll("danger-button", "card-action");
            delete.setOnAction(e -> deleteAnswer(reponse));

            VBox actions = new VBox(8, edit, delete);
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
        return text == null || text.isBlank() ? "(sans texte)" : text;
    }
}
