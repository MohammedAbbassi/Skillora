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
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;

import javafx.geometry.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CoursListController {

    @FXML private FlowPane coursesContainer;
    @FXML private Button btnNewCours;
    @FXML private TextField searchField;

    private CoursService coursService = new CoursService();
    private services.CertificateService certificateService = new services.CertificateService();
    private List<Cours> allCourses;

    @FXML
    public void initialize() {
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        if (btnNewCours != null) btnNewCours.setVisible(isAdmin);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCourses(newVal);
        });
        
        loadCourses();
    }

    private void loadCourses() {
        try {
            allCourses = coursService.getAll();
            renderCourses(allCourses);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterCourses(String query) {
        if (query == null || query.isEmpty()) {
            renderCourses(allCourses);
            return;
        }
        String q = query.toLowerCase();
        List<Cours> filtered = allCourses.stream()
            .filter(c -> c.getTitre().toLowerCase().contains(q) || 
                         (c.getCategorie() != null && c.getCategorie().toLowerCase().contains(q)))
            .collect(java.util.stream.Collectors.toList());
        renderCourses(filtered);
    }

    private void renderCourses(List<Cours> courses) {
        coursesContainer.getChildren().clear();
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        for (Cours cours : courses) {
            VBox card = createCourseCard(cours, isAdmin);
            coursesContainer.getChildren().add(card);
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

        FlowPane tagsBox = new FlowPane();
        tagsBox.setHgap(5);
        tagsBox.setVgap(5);
        if (cours.getCategorie() != null && !cours.getCategorie().isEmpty()) {
            String[] tags = cours.getCategorie().split(",\\s*");
            for (String tag : tags) {
                Label tagLabel = new Label(tag);
                tagLabel.getStyleClass().add("chip");
                tagLabel.setStyle("-fx-font-size: 10px; -fx-padding: 4 8;");
                tagsBox.getChildren().add(tagLabel);
            }
        } else {
            Label cat = new Label("Aucune catégorie");
            cat.getStyleClass().add("chip");
            cat.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");
            tagsBox.getChildren().add(cat);
        }
        
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

        Button planBtn = new Button("Voir le plan");
        planBtn.getStyleClass().add("btn-secondary");
        planBtn.setMaxWidth(Double.MAX_VALUE);
        planBtn.setOnAction(e -> handleViewChapters(cours));

        card.getChildren().addAll(tagsBox, title, level, duration, desc, extraInfo, planBtn);


        // Certificate & Quiz Buttons
        if (cours.getProgression() == 100) {
            HBox completedActions = new HBox(10);
            completedActions.setAlignment(Pos.CENTER);

            Button certBtn = new Button("🎓 Certificat");
            certBtn.getStyleClass().add("btn-primary");
            certBtn.setStyle("-fx-background-color: #8b5cf6;");
            certBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(certBtn, Priority.ALWAYS);
            certBtn.setOnAction(e -> handleGenerateCertificate(cours));

            Button quizBtn = new Button("📝 Passer au Quiz");
            quizBtn.getStyleClass().add("btn-primary");
            quizBtn.setStyle("-fx-background-color: #10b981;");
            quizBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(quizBtn, Priority.ALWAYS);
            quizBtn.setOnAction(e -> handleViewQuiz(cours));

            completedActions.getChildren().addAll(certBtn, quizBtn);
            card.getChildren().add(completedActions);
        }


        // Navigation on card click
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                handleViewChapters(cours);
            }
        });

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
    
    private void handleViewChapters(Cours cours) {
        ChapitreListController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-list.fxml");
        if (ctrl != null) {
            ctrl.setCourseContext(cours);
        }
    }

    private void handleViewQuiz(Cours cours) {
        MainLayoutController.getInstance().loadView("/ui/quiz-view.fxml");
    }


    private void handleGenerateCertificate(Cours cours) {

        try {
            // In a real app, we would get the student name from the session
            String studentName = "Étudiant Skillora";
            String path = certificateService.generateCertificate(cours, studentName);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Certificat Généré");
            alert.setHeaderText("Félicitations !");
            alert.setContentText("Votre certificat a été généré avec succès :\n" + path);
            alert.showAndWait();
            
            // Try to open it
            try {
                java.io.File file = new java.io.File(path);
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                System.err.println("Could not open PDF automatically: " + ex.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Erreur lors de la génération du certificat : " + e.getMessage());
            errorAlert.showAndWait();
        }
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
