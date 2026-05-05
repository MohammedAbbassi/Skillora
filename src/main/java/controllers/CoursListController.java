package controllers;

import entities.Cours;
import services.CoursService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CoursListController {

    @FXML private FlowPane coursesContainer;
    @FXML private Button btnNewCours;

    private CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        if (btnNewCours != null) btnNewCours.setVisible(isAdmin);
        loadCourses();
    }

    private void loadCourses() {
        coursesContainer.getChildren().clear();
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        try {
            List<Cours> courses = coursService.getAll();
            for (Cours cours : courses) {
                VBox card = createCourseCard(cours, isAdmin);
                coursesContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCourseCard(Cours cours, boolean isAdmin) {
        VBox card = new VBox();
        card.getStyleClass().add("course-card");
        card.setPrefWidth(300);
        card.setSpacing(10);

        Label title = new Label(cours.getTitre());
        title.getStyleClass().add("course-card-title");
        title.setWrapText(true);

        Label cat = new Label(cours.getCategorie() != null ? cours.getCategorie() : "Aucune");
        cat.getStyleClass().add("chip");
        
        Label level = new Label(cours.getNiveau());
        level.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Label desc = new Label(cours.getDescription() != null ? cours.getDescription() : "");
        desc.getStyleClass().add("course-card-desc");
        desc.setMaxHeight(60);
        desc.setWrapText(true);

        Label duration = new Label("Durée : " + (cours.getDuree() != null ? cours.getDuree() : "?"));
        duration.getStyleClass().add("course-card-subtitle");
        duration.setStyle("-fx-font-weight: bold; -fx-text-fill: #0ea5e9;");

        VBox extraInfo = new VBox(5);
        extraInfo.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");
        extraInfo.setPadding(new Insets(10));
        
        Label objLabel = new Label("Objectif : " + (cours.getObjectifSemaine() != null ? cours.getObjectifSemaine() : "Non défini"));
        objLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        objLabel.setWrapText(true);
        
        Label perfLabel = new Label("Performance : " + (cours.getPerformance() != null ? cours.getPerformance() : "N/A"));
        perfLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0f172a; -fx-font-weight: bold;");

        ProgressBar pb = new ProgressBar((double)cours.getProgression() / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar-custom");
        Label progText = new Label("Progression : " + cours.getProgression() + "%");
        progText.setStyle("-fx-font-size: 10px;");

        extraInfo.getChildren().addAll(objLabel, perfLabel, progText, pb);

        card.getChildren().addAll(cat, title, level, duration, desc, extraInfo);

        if (isAdmin) {
            // Action Buttons
            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().add("btn-secondary");
            editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
            editBtn.setOnAction(e -> handleEdit(cours));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fca5a5; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> handleDelete(cours));
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            HBox actionsBox = new HBox(10, editBtn, spacer, deleteBtn);
            actionsBox.setAlignment(Pos.CENTER_LEFT);
            actionsBox.setStyle("-fx-padding: 10 0 0 0;");
            card.getChildren().add(actionsBox);
        }

        return card;
    }
    
    private void handleEdit(Cours cours) {
        CoursFormController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/cours-form.fxml");
        if (ctrl != null) {
            ctrl.setCoursForEdit(cours);
        }
    }
    
    private void handleDelete(Cours cours) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le cours ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le cours : " + cours.getTitre() + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coursService.delete(cours);
                loadCourses(); // Refresh
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setContentText("Erreur lors de la suppression : " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    void onNewCours(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/cours-form.fxml");
    }
}
