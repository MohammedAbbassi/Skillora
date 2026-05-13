package controllers;

import entities.Quiz;
import services.PdfExportService;
import services.QuestionCRUD;
import services.QuizCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class QuizManagementController {

    @FXML private ListView<Quiz> quizList;

    @FXML private TextField searchTitreField;
    @FXML private ComboBox<String> niveauTriCombo;
    @FXML private ComboBox<Quiz.Matiere> searchMatiereCombo;
    @FXML private ComboBox<String> dateTriCombo;
    @FXML private CheckBox corrigeCheck;
    @FXML private Label searchCountLabel;

    private final QuizCRUD quizCRUD = new QuizCRUD();
    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final PdfExportService pdfExportService = new PdfExportService();
    private final Map<Integer, Integer> questionCounts = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        if (niveauTriCombo != null) {
            niveauTriCombo.setItems(FXCollections.observableArrayList(
                    "Tous", "Debutant", "Intermediaire", "Avance", "Expert"
            ));
            niveauTriCombo.setValue("Tous");
            niveauTriCombo.valueProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (searchMatiereCombo != null) {
            searchMatiereCombo.setItems(FXCollections.observableArrayList(Quiz.Matiere.values()));
        }
        if (dateTriCombo != null) {
            dateTriCombo.setItems(FXCollections.observableArrayList(
                    "Plus recents", "Plus anciens"
            ));
            dateTriCombo.setValue("Plus recents");
            dateTriCombo.valueProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (searchTitreField != null) {
            searchTitreField.textProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (searchMatiereCombo != null) {
            searchMatiereCombo.valueProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (quizList != null) {
            quizList.setCellFactory(lv -> new QuizCardCell());
            quizList.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    Quiz q = quizList.getSelectionModel().getSelectedItem();
                    if (q != null) {
                        AppNavigator.setEditQuizId(q.getId());
                        AppNavigator.load("AjouterQuiz.fxml");
                    }
                }
            });
        }
        refresh();
    }

    @FXML
    void addQuiz() {
        AppNavigator.setEditQuizId(0);
        AppNavigator.load("AjouterQuiz.fxml");
    }

    @FXML
    void editQuiz() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }
        AppNavigator.setEditQuizId(quiz.getId());
        AppNavigator.load("AjouterQuiz.fxml");
    }

    @FXML
    void deleteQuiz() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }
        deleteQuiz(quiz);
    }

    private void deleteQuiz(Quiz quiz) {
        if (!confirmDelete("Supprimer le quiz",
                "Voulez-vous vraiment supprimer ce quiz ?",
                "Quiz : " + safeText(quiz.getNomQuiz())
                        + "\nToutes les questions et reponses liees peuvent aussi etre supprimees.")) {
            return;
        }
        try {
            quizCRUD.supprimerQuiz(quiz.getId());
            refresh();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void addQuestion() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }
        AppNavigator.setSelectedQuizId(quiz.getId());
        AppNavigator.load("AjouterQuestion.fxml");
    }

    @FXML
    void generateWithAi() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }
        AppNavigator.setSelectedQuizId(quiz.getId());
        AppNavigator.load("AiQuizGenerator.fxml");
    }

    @FXML
    void passQuiz() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }
        AppNavigator.setSelectedQuizId(quiz.getId());
        AppNavigator.load("PasserQuiz.fxml");
    }

    @FXML
    void exportQuizPdf() {
        Quiz quiz = selectedQuiz();
        if (quiz == null) {
            return;
        }

        try {
            File file = pdfExportService.exportQuiz(quiz, corrigeCheck != null && corrigeCheck.isSelected());
            alert("PDF genere", "Votre quiz est exporte en PDF.");
            pdfExportService.openExportLocation(file);
        } catch (Exception e) {
            alert("Export PDF impossible", e.getMessage());
        }
    }

    private void refresh() {
        try {
            String titre = searchTitreField == null ? null : searchTitreField.getText();
            String niveauFiltre = null;
            if (niveauTriCombo != null && niveauTriCombo.getValue() != null) {
                String v = niveauTriCombo.getValue();
                if (!"Tous".equalsIgnoreCase(v)) {
                    niveauFiltre = v;
                }
            }
            String matiere = null;
            if (searchMatiereCombo != null && searchMatiereCombo.getValue() != null) {
                matiere = searchMatiereCombo.getValue().name();
            }
            boolean dateDesc = dateTriCombo == null
                    || dateTriCombo.getValue() == null
                    || "Plus recents".equalsIgnoreCase(dateTriCombo.getValue());

            var items = FXCollections.observableArrayList(
                    quizCRUD.rechercherQuizzes(titre, matiere, niveauFiltre, dateDesc)
            );

            questionCounts.clear();
            for (Quiz quiz : items) {
                questionCounts.put(quiz.getId(), questionCRUD.compterParQuiz(quiz.getId()));
            }

            quizList.setItems(items);
            if (searchCountLabel != null) {
                searchCountLabel.setText(items.size() + " r\u00e9sultat(s)");
            }
            quizList.refresh();
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private Quiz selectedQuiz() {
        Quiz quiz = quizList.getSelectionModel().getSelectedItem();
        if (quiz == null) {
            alert("Sélection requise", "Sélectionnez un quiz dans le tableau.");
        }
        return quiz;
    }

    private class QuizCardCell extends ListCell<Quiz> {
        @Override
        protected void updateItem(Quiz quiz, boolean empty) {
            super.updateItem(quiz, empty);
            if (empty || quiz == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(quiz.getNomQuiz() == null ? "" : quiz.getNomQuiz());
            title.getStyleClass().add("card-title");

            String metaText = (quiz.getMatiereValue() == null ? "" : quiz.getMatiereValue()) +
                    "  -  " + (quiz.getNiveau() == null ? "" : quiz.getNiveau());
            Label meta = new Label(metaText);
            meta.getStyleClass().add("card-meta");

            int questionCount = questionCounts.getOrDefault(quiz.getId(), 0);
            Label questionCountLabel = new Label(questionCount + (questionCount == 1 ? " question" : " questions"));
            questionCountLabel.getStyleClass().add("card-meta");

            String dateText = quiz.getDateAjout() == null
                    ? "Date d'ajout non definie"
                    : "Ajoute le " + quiz.getDateAjout().format(DATE_FORMATTER);
            Label date = new Label(dateText);
            date.getStyleClass().add("card-meta");

            String descText = quiz.getDescription() == null ? "" : quiz.getDescription().trim();
            Label desc = new Label(descText);
            desc.setWrapText(true);
            desc.getStyleClass().add("card-desc");

            VBox textBox = new VBox(4, title, meta, questionCountLabel, date, desc);
            textBox.getStyleClass().add("card-content");

            Button details = new Button("Voir details");
            details.getStyleClass().addAll("secondary-button", "card-action");
            details.setOnAction(e -> {
                AppNavigator.setSelectedQuizId(quiz.getId());
                AppNavigator.load("PasserQuiz.fxml");
            });

            Button edit = new Button("Modifier");
            edit.getStyleClass().addAll("secondary-button", "card-action");
            edit.setOnAction(e -> {
                AppNavigator.setEditQuizId(quiz.getId());
                AppNavigator.load("AjouterQuiz.fxml");
            });

            Button delete = new Button("Supprimer");
            delete.getStyleClass().addAll("danger-button", "card-action");
            delete.setOnAction(e -> deleteQuiz(quiz));

            VBox actions = new VBox(8, details, edit, delete);
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
        return text == null || text.isBlank() ? "(sans titre)" : text;
    }
}
