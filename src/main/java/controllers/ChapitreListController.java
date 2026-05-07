package controllers;

import entities.Chapitre;
import entities.Cours;
import services.ChapitreService;
import services.CoursService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.util.StringConverter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import java.sql.SQLException;
import java.util.List;

public class ChapitreListController {

    @FXML private ComboBox<Cours> courseSelector;
    @FXML private TextField searchField;
    @FXML private FlowPane chaptersContainer;
    @FXML private Button btnAddChapitre;

    private ChapitreService chapitreService = new ChapitreService();
    private CoursService coursService = new CoursService();
    private ObservableList<Chapitre> allChapters = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        if (btnAddChapitre != null) btnAddChapitre.setVisible(isAdmin);
        
        setupCourseSelector();
        
        courseSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadChapters(newVal.getIdCours());
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterChapters(newVal);
        });
    }

    public void setCourseContext(Cours cours) {
        if (cours != null) {
            courseSelector.setValue(cours);
            loadChapters(cours.getIdCours());
        }
    }

    private void setupCourseSelector() {

        try {
            List<Cours> courses = coursService.getAll();
            courseSelector.setItems(FXCollections.observableArrayList(courses));
            courseSelector.setConverter(new StringConverter<Cours>() {
                @Override public String toString(Cours c) { return c == null ? "" : c.getTitre(); }
                @Override public Cours fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadChapters(int courseId) {
        try {
            List<Chapitre> chapters = chapitreService.getByCours(courseId);
            allChapters.setAll(chapters);
            renderChapters(allChapters);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void filterChapters(String query) {
        if (query == null || query.isEmpty()) {
            renderChapters(allChapters);
            return;
        }
        FilteredList<Chapitre> filtered = new FilteredList<>(allChapters, 
            ch -> ch.getTitre().toLowerCase().contains(query.toLowerCase()) || 
                  (ch.getResume() != null && ch.getResume().toLowerCase().contains(query.toLowerCase())));
        renderChapters(filtered);
    }

    private void renderChapters(List<Chapitre> chapters) {
        chaptersContainer.getChildren().clear();
        boolean isAdmin = MainLayoutController.getInstance().isAdminMode();
        for (Chapitre ch : chapters) {
            chaptersContainer.getChildren().add(createChapterCard(ch, isAdmin));
        }
    }

    private VBox createChapterCard(Chapitre ch, boolean isAdmin) {
        VBox card = new VBox(0);
        card.getStyleClass().add("chapter-card");
        card.setPrefWidth(300);

        // Thumbnail Placeholder
        StackPane thumb = new StackPane();
        thumb.getStyleClass().add("chapter-thumb");
        Label thumbLabel = new Label("Chapitre " + ch.getOrdre());
        thumbLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        thumb.getChildren().add(thumbLabel);

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(ch.getNiveau() != null ? ch.getNiveau() : "MOYEN");
        badge.getStyleClass().add("badge-" + badge.getText().toLowerCase());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label duration = new Label(ch.getDuree() + " min");
        duration.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        top.getChildren().addAll(badge, spacer, duration);

        Label title = new Label(ch.getTitre());
        title.getStyleClass().add("course-card-title");
        title.setWrapText(true);
        title.setMinHeight(50);

        Label resume = new Label(ch.getResume());
        resume.getStyleClass().add("course-card-desc");
        resume.setMaxHeight(40);
        resume.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setPrefWidth(100);
        viewBtn.setOnAction(e -> handleViewDetail(ch));

        footer.getChildren().addAll(viewBtn);


        if (isAdmin) {
            Button editBtn = new Button("Editer");
            editBtn.getStyleClass().add("btn-secondary");
            editBtn.setOnAction(e -> handleEdit(ch));
            footer.getChildren().add(editBtn);
        }

        content.getChildren().addAll(top, title, resume, footer);
        card.getChildren().addAll(thumb, content);
        
        return card;
    }

    @FXML
    void onBackToCourses(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/cours-list.fxml");
    }

    @FXML
    void onAddChapitre(ActionEvent event) {

        Cours selected = courseSelector.getValue();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez d'abord sélectionner un cours.");
            alert.show();
            return;
        }
        ChapitreAdminController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-admin.fxml");
        if (ctrl != null) {
            ctrl.setCourseContext(selected);
        }
    }

    private void handleEdit(Chapitre ch) {
        ChapitreAdminController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-admin.fxml");
        if (ctrl != null) {
            ctrl.setChapterForEdit(ch, courseSelector.getValue());
        }
    }

    private void handleViewDetail(Chapitre ch) {
        ChapitreDetailController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-detail.fxml");
        if (ctrl != null) {
            ctrl.setChapter(ch, allChapters);
        }
    }
}
