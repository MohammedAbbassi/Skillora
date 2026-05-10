package controllers;

import entities.Quiz;
import services.QuestionCRUD;
import services.QuizCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserQuizSelectionController {

    @FXML private ListView<Quiz> quizListView;

    @FXML private TextField searchTitreField;
    @FXML private TextField searchNiveauField;
    @FXML private TextField searchMatiereField;
    @FXML private Label searchCountLabel;

    private final QuizCRUD quizCRUD = new QuizCRUD();
    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final Map<Integer, Integer> questionCounts = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            setupQuizListView();

            if (searchTitreField != null) {
                searchTitreField.textProperty().addListener((obs, oldValue, newValue) -> safeRefreshList());
            }
            if (searchNiveauField != null) {
                searchNiveauField.textProperty().addListener((obs, oldValue, newValue) -> safeRefreshList());
            }
            if (searchMatiereField != null) {
                searchMatiereField.textProperty().addListener((obs, oldValue, newValue) -> safeRefreshList());
            }

            refreshList();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void setupQuizListView() {
        Label emptyLabel = new Label("Aucun quiz disponible pour ces criteres.");
        emptyLabel.getStyleClass().add("page-subtitle");
        quizListView.setPlaceholder(emptyLabel);
        quizListView.setCellFactory(listView -> new QuizListCell());
    }

    private void safeRefreshList() {
        try {
            refreshList();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private void refreshList() throws SQLException {
        String titre = searchTitreField == null ? null : searchTitreField.getText();
        String niveau = searchNiveauField == null ? null : searchNiveauField.getText();
        String matiere = searchMatiereField == null ? null : searchMatiereField.getText();

        var items = FXCollections.observableArrayList(quizCRUD.rechercherQuizzes(titre, niveau, matiere));
        Quiz current = quizListView.getSelectionModel().getSelectedItem();

        questionCounts.clear();
        for (Quiz quiz : items) {
            questionCounts.put(quiz.getId(), questionCRUD.compterParQuiz(quiz.getId()));
        }

        quizListView.setItems(items);

        if (current != null) {
            for (Quiz quiz : items) {
                if (quiz.getId() == current.getId()) {
                    quizListView.getSelectionModel().select(quiz);
                    break;
                }
            }
        }

        if (searchCountLabel != null) {
            searchCountLabel.setText(items.size() + " quiz");
        }

        quizListView.refresh();
    }

    private void commencerQuiz(Quiz quiz) {
        if (quiz == null) {
            alert("Quiz requis", "Selectionnez un quiz avant de commencer.");
            return;
        }
        AppNavigator.setSelectedQuizId(quiz.getId());
        AppNavigator.load("PasserQuiz.fxml");
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final class QuizListCell extends ListCell<Quiz> {

        private final VBox card = new VBox(10);
        private final Label titleLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final FlowPane metaPane = new FlowPane(8, 8);
        private final Label matiereLabel = new Label();
        private final Label niveauLabel = new Label();
        private final Label questionsLabel = new Label();
        private final Button startButton = new Button("Commencer");

        private QuizListCell() {
            titleLabel.getStyleClass().add("quiz-card-title");
            titleLabel.setWrapText(true);

            descriptionLabel.getStyleClass().add("quiz-card-desc");
            descriptionLabel.setWrapText(true);

            matiereLabel.getStyleClass().add("quiz-chip");
            niveauLabel.getStyleClass().add("quiz-chip");
            questionsLabel.getStyleClass().add("quiz-chip");
            metaPane.getStyleClass().add("quiz-card-meta");
            metaPane.getChildren().setAll(matiereLabel, niveauLabel, questionsLabel);

            startButton.getStyleClass().addAll("primary-button", "quiz-start-button");
            startButton.setOnAction(event -> {
                event.consume();
                commencerQuiz(getItem());
            });

            Region spacer = new Region();
            HBox actionRow = new HBox(spacer, startButton);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            actionRow.getStyleClass().add("quiz-card-actions");

            card.getStyleClass().add("quiz-card");
            card.getChildren().setAll(titleLabel, descriptionLabel, metaPane, actionRow);
            card.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && getItem() != null && !isInsideButton(event.getTarget())) {
                    commencerQuiz(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Quiz quiz, boolean empty) {
            super.updateItem(quiz, empty);

            if (empty || quiz == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            titleLabel.setText(isBlank(quiz.getNomQuiz()) ? "Quiz sans titre" : quiz.getNomQuiz());

            boolean hasDescription = !isBlank(quiz.getDescription());
            descriptionLabel.setText(hasDescription ? quiz.getDescription().trim() : "");
            descriptionLabel.setVisible(hasDescription);
            descriptionLabel.setManaged(hasDescription);

            matiereLabel.setText(isBlank(quiz.getMatiereValue()) ? "Matiere non definie" : quiz.getMatiereValue());
            niveauLabel.setText(isBlank(quiz.getNiveau()) ? "Niveau non defini" : quiz.getNiveau().trim());

            int questionCount = questionCounts.getOrDefault(quiz.getId(), 0);
            questionsLabel.setText(questionCount + (questionCount == 1 ? " question" : " questions"));

            setText(null);
            setGraphic(card);
        }

        private boolean isInsideButton(Object target) {
            if (!(target instanceof Node)) {
                return false;
            }

            Node node = (Node) target;
            while (node != null) {
                if (node instanceof Button) {
                    return true;
                }
                node = node.getParent();
            }
            return false;
        }

        private boolean isBlank(String value) {
            return value == null || value.isBlank();
        }
    }
}
