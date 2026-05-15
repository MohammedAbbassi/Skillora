package controllers;

import javafx.collections.FXCollections;
import entities.Cours;
import entities.Role;
import entities.User;
import entities.Chapitre;
import services.CoursService;
import services.NavigationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import utils.SessionManager;

public class CoursListController {

    @FXML private FlowPane coursesContainer;
    @FXML private Button btnNewCours;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;

    private CoursService coursService = new CoursService();
    private NavigationService navigationService = new NavigationService();
    private List<Cours> allCourses;
    private Chapitre recommendedChapter;

    @FXML private VBox navigationAssistantCard;
    @FXML private Label guidanceMessage;
    @FXML private HBox recommendationBox;
    @FXML private Button btnNextChapter;

    @FXML
    public void initialize() {
        boolean isAdmin = isAdmin();
        if (btnNewCours != null) btnNewCours.setVisible(isAdmin);
        
        // Setup filters
        levelFilter.setItems(FXCollections.observableArrayList("Tous les niveaux", "DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        levelFilter.setValue("Tous les niveaux");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        levelFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        loadCourses();
    }

    private boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getRole() == null) return false;
        Role userRole = Role.fromString(user.getRole());
        return userRole == Role.ADMIN || userRole == Role.INSTRUCTEUR;
    }

    private void loadCourses() {
        try {
            allCourses = coursService.getAll();
            renderCourses(allCourses);
            updateNavigationAssistant();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateNavigationAssistant() {
        if (allCourses == null || allCourses.isEmpty() || isAdmin()) {
            navigationAssistantCard.setVisible(false);
            navigationAssistantCard.setManaged(false);
            return;
        }

        // Guide based on the first active course for now
        Cours targetCourse = allCourses.get(0);
        String guidance = navigationService.getSmartGuidance(targetCourse);
        guidanceMessage.setText(guidance);
        
        Optional<Chapitre> next = navigationService.getRecommendedNext(targetCourse);
        if (next.isPresent()) {
            recommendedChapter = next.get();
            btnNextChapter.setText(recommendedChapter.getTitre());
            recommendationBox.setVisible(true);
            recommendationBox.setManaged(true);
        } else {
            recommendationBox.setVisible(false);
            recommendationBox.setManaged(false);
        }
        
        navigationAssistantCard.setVisible(true);
        navigationAssistantCard.setManaged(true);
    }

    @FXML
    void onDismissAssistant(ActionEvent event) {
        navigationAssistantCard.setVisible(false);
        navigationAssistantCard.setManaged(false);
    }

    @FXML
    void onGoToRecommended(ActionEvent event) {
        if (recommendedChapter != null) {
            ChapitreDetailController ctrl = MainController.getInstance().loadViewIntoPage("/ui/chapitre-detail.fxml", (VBox) coursesContainer.getScene().lookup("#pageCourses"));
            if (ctrl != null) {
                ctrl.setChapter(recommendedChapter, null);
            }
        }
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();
        String level = levelFilter.getValue();

        List<Cours> filtered = allCourses.stream()
            .filter(c -> c.getTitre().toLowerCase().contains(query))
            .filter(c -> level.equals("Tous les niveaux") || (c.getNiveau() != null && c.getNiveau().equalsIgnoreCase(level)))
            .collect(java.util.stream.Collectors.toList());
            
        renderCourses(filtered);
    }

    private void renderCourses(List<Cours> courses) {
        coursesContainer.getChildren().clear();
        boolean isAdmin = isAdmin();
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
        
        extraInfo.getChildren().addAll(objLabel);

        Button planBtn = new Button("Voir le plan");
        planBtn.getStyleClass().add("btn-secondary");
        planBtn.setMaxWidth(Double.MAX_VALUE);
        planBtn.setOnAction(e -> handleViewChapters(cours));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        if (cours.getImageUrl() != null && !cours.getImageUrl().isEmpty()) {
            try {
                imageView.setImage(new javafx.scene.image.Image(cours.getImageUrl(), true));
            } catch (Exception ignored) {}
        } else {
            imageView.setStyle("-fx-background-color: #e2e8f0;");
        }
        
        card.getChildren().addAll(imageView, tagsBox, title, level, duration, desc, extraInfo, planBtn);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                handleViewChapters(cours);
            }
        });

        if (isAdmin) {
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
        ChapitreListController ctrl = MainController.getInstance().loadViewIntoPage("/ui/chapitre-list.fxml", (VBox) coursesContainer.getScene().lookup("#pageCourses"));
        if (ctrl != null) {
            ctrl.setCourseContext(cours);
        }
    }

    private void handleEdit(Cours cours) {
        CoursFormController ctrl = MainController.getInstance().loadViewIntoPage("/ui/cours-form.fxml", (VBox) coursesContainer.getScene().lookup("#pageCourses"));
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
                loadCourses();
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
        MainController.getInstance().loadViewIntoPage("/ui/cours-form.fxml", (VBox) coursesContainer.getScene().lookup("#pageCourses"));
    }
}
