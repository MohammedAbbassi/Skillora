package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainLayoutController {

    @FXML private Button btnCours;
    @FXML private Button btnChapitres;
    @FXML private ToggleButton toggleAdmin;
    @FXML private StackPane contentArea;

    private static MainLayoutController instance;
    private boolean isAdminMode = false;

    @FXML
    public void initialize() {
        instance = this;
        isAdminMode = false;
        // Par défaut on affiche la liste des cours
        loadView("/ui/cours-list.fxml");
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public boolean isAdminMode() {
        return isAdminMode;
    }

    @FXML
    void onToggleAdmin(ActionEvent event) {
        this.isAdminMode = toggleAdmin.isSelected();
        if (isAdminMode) {
            toggleAdmin.setText("Mode ADMIN : ON");
            toggleAdmin.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171;");
        } else {
            toggleAdmin.setText("Mode Administrateur");
            toggleAdmin.setStyle("");
        }
        // Rafraîchir la vue actuelle
        if (btnCours.getStyleClass().contains("sidebar-btn-active")) onCoursClick(null);
        else onChapitresClick(null);
    }

    @FXML
    void onCoursClick(ActionEvent event) {
        // Active state styling
        btnCours.getStyleClass().add("sidebar-btn-active");
        btnChapitres.getStyleClass().remove("sidebar-btn-active");
        
        loadView("/ui/cours-list.fxml");
    }

    @FXML
    void onChapitresClick(ActionEvent event) {
        btnChapitres.getStyleClass().add("sidebar-btn-active");
        btnCours.getStyleClass().remove("sidebar-btn-active");
        
        loadView("/ui/chapitre-list.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T loadViewAndGetController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

