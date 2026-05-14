package controllers;

import entities.QuizResult;
import entities.QuizResultDetail;
import services.PdfExportService;
import services.QuizResultHistoryService;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class QuizHistoryController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> antiCheatFilterCombo;
    @FXML private Label searchCountLabel;
    @FXML private ListView<QuizResult> resultList;

    private final QuizResultHistoryService historyService = new QuizResultHistoryService();
    private final PdfExportService pdfExportService = new PdfExportService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        if (antiCheatFilterCombo != null) {
            antiCheatFilterCombo.setItems(FXCollections.observableArrayList(
                    "Tous", "Avec incidents", "Sans incidents", "Termines anti-triche"
            ));
            antiCheatFilterCombo.setValue("Tous");
            antiCheatFilterCombo.valueProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> refresh());
        }
        if (resultList != null) {
            resultList.setCellFactory(list -> new ResultCardCell());
            resultList.setPlaceholder(new Label("Aucun passage de quiz enregistre."));
        }
        refresh();
    }

    @FXML
    void refreshHistory() {
        refresh();
    }

    @FXML
    void exportSelectedResult() {
        QuizResult result = selectedResult();
        if (result == null) {
            return;
        }
        exportResult(result);
    }

    private void refresh() {
        try {
            String search = searchField == null ? null : searchField.getText();
            String antiCheatFilter = antiCheatFilterCombo == null ? "Tous" : antiCheatFilterCombo.getValue();
            var items = FXCollections.observableArrayList(historyService.searchResults(search, antiCheatFilter));
            resultList.setItems(items);
            if (searchCountLabel != null) {
                searchCountLabel.setText(items.size() + " passage(s)");
            }
        } catch (SQLException e) {
            alert("Erreur SQL", e.getMessage());
        }
    }

    private QuizResult selectedResult() {
        QuizResult result = resultList.getSelectionModel().getSelectedItem();
        if (result == null) {
            alert("Selection requise", "Selectionnez un resultat dans l'historique.");
        }
        return result;
    }

    private void exportResult(QuizResult result) {
        try {
            File file = pdfExportService.exportResult(result);
            alert("PDF genere", "Le rapport du passage selectionne est exporte en PDF.");
            pdfExportService.openExportLocation(file);
        } catch (Exception e) {
            alert("Export PDF impossible", e.getMessage());
        }
    }

    private void showDetails(QuizResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details du passage");
        alert.setHeaderText(result.getQuizTitle());

        TextArea area = new TextArea(buildDetailsText(result));
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(680);
        area.setPrefHeight(460);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private String buildDetailsText(QuizResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Utilisateur: ").append(textOrFallback(result.getUserName(), "Non disponible")).append('\n');
        builder.append("Score: ").append(result.getFinalPoints()).append(" / ").append(result.getTotalPoints()).append(" pts\n");
        builder.append("Bonnes reponses: ").append(result.getCorrectAnswers()).append(" / ").append(result.getTotalQuestions()).append('\n');
        builder.append("Bonus rapidite: +").append(result.getBonusPoints()).append('\n');
        builder.append("Date: ").append(result.getCompletedAt() == null ? "Non disponible" : result.getCompletedAt().format(DATE_FORMATTER)).append('\n');
        builder.append("Anti-triche: ").append(textOrFallback(result.getAntiCheatSummary(), "Aucun resume")).append('\n');
        builder.append("Incidents critiques: ").append(result.getAntiCheatCriticalCount()).append('\n');
        if (result.isTerminatedByAntiCheat()) {
            builder.append("Statut: termine automatiquement par anti-triche\n");
        }
        if (result.getAntiCheatReportPath() != null && !result.getAntiCheatReportPath().isBlank()) {
            builder.append("Rapport anti-triche: ").append(result.getAntiCheatReportPath()).append('\n');
        }

        builder.append("\nQuestions:\n");
        int index = 1;
        for (QuizResultDetail detail : result.getDetails()) {
            builder.append('\n').append(index++).append(". ").append(textOrFallback(detail.getQuestionText(), "Question")).append('\n');
            builder.append("Type: ").append(textOrFallback(detail.getQuestionType(), "-")).append('\n');
            builder.append("Reponse donnee: ").append(join(detail.getSelectedAnswers())).append('\n');
            builder.append("Bonne reponse: ").append(join(detail.getCorrectAnswers())).append('\n');
            builder.append("Statut: ").append(detail.isCorrect() ? "Correct" : "Incorrect").append('\n');
        }
        if (result.getDetails().isEmpty()) {
            builder.append("Aucun detail question par question disponible.\n");
        }
        return builder.toString();
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String textOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String join(java.util.List<String> values) {
        return values == null || values.isEmpty() ? "Aucune reponse" : String.join(", ", values);
    }

    private final class ResultCardCell extends ListCell<QuizResult> {
        @Override
        protected void updateItem(QuizResult result, boolean empty) {
            super.updateItem(result, empty);
            if (empty || result == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(textOrFallback(result.getQuizTitle(), "Quiz"));
            title.getStyleClass().add("card-title");

            String date = result.getCompletedAt() == null
                    ? "Date non disponible"
                    : result.getCompletedAt().format(DATE_FORMATTER);
            Label meta = new Label(textOrFallback(result.getUserName(), "Utilisateur")
                    + "  -  " + date);
            meta.getStyleClass().add("card-meta");

            Label score = new Label("Score: " + result.getFinalPoints() + " / " + result.getTotalPoints()
                    + " pts  -  Bonnes reponses: " + result.getCorrectAnswers() + " / " + result.getTotalQuestions());
            score.getStyleClass().add("card-meta");

            String antiCheatText = result.getAntiCheatIncidentCount() == 0
                    ? "Anti-triche: aucun incident"
                    : "Anti-triche: " + result.getAntiCheatIncidentCount() + " incident(s), "
                    + result.getAntiCheatCriticalCount() + " critique(s)";
            if (result.isTerminatedByAntiCheat()) {
                antiCheatText += " - termine automatiquement";
            }
            Label antiCheat = new Label(antiCheatText);
            antiCheat.getStyleClass().add(result.getAntiCheatIncidentCount() == 0 ? "card-pill-ok" : "card-pill-bad");

            VBox textBox = new VBox(5, title, meta, score, antiCheat);
            textBox.getStyleClass().add("card-content");

            Button details = new Button("Details");
            details.getStyleClass().addAll("secondary-button", "card-action");
            details.setOnAction(e -> showDetails(result));

            Button export = new Button("Exporter PDF");
            export.getStyleClass().addAll("secondary-button", "card-action");
            export.setOnAction(e -> exportResult(result));

            VBox actions = new VBox(8, details, export);
            actions.getStyleClass().add("card-actions");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox root = new HBox(14, textBox, spacer, actions);
            root.getStyleClass().add("list-card");

            setText(null);
            setGraphic(root);
        }
    }
}
