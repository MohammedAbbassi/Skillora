package com.skillora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import entities.Role;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane appRoot;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnNavEvenements;
    @FXML
    private Button btnNavReservations;
    @FXML
    private ComboBox<Role> comboRoleTest;
    @FXML
    private Label lblSessionUser;
    @FXML
    private ToggleButton btnDyslexiaMode;
    @FXML
    private VBox accessibilityPanel;

    private String currentView = "/com/skillora/views/EvenementInterface.fxml";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRoleTester();
        applyNavigationForCurrentRole();
        showDefaultViewForCurrentRole();
    }

    private void setupRoleTester() {
        comboRoleTest.getItems().setAll(Role.ADMIN, Role.INSTRUCTEUR, Role.ETUDIANT);
        comboRoleTest.setConverter(new StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                if (role == Role.ADMIN) {
                    return "Admin";
                }
                if (role == Role.INSTRUCTEUR) {
                    return "Enseignant";
                }
                if (role == Role.ETUDIANT) {
                    return "Etudiant";
                }
                return "";
            }

            @Override
            public Role fromString(String value) {
                if ("Admin".equals(value)) {
                    return Role.ADMIN;
                }
                if ("Enseignant".equals(value)) {
                    return Role.INSTRUCTEUR;
                }
                if ("Etudiant".equals(value)) {
                    return Role.ETUDIANT;
                }
                return null;
            }
        });
        comboRoleTest.setValue(SessionManager.getCurrentUserRole());
        updateSessionLabel();
    }

    @FXML
    private void showEvenements() {
        if (!canOpenEvenementsPage()) {
            showReservations();
            return;
        }
        setActiveButton(btnNavEvenements);
        currentView = "/com/skillora/views/EvenementInterface.fxml";
        loadView(currentView);
    }

    @FXML
    private void showReservations() {
        setActiveButton(btnNavReservations);
        currentView = "/com/skillora/views/ReservationInterface.fxml";
        loadView(currentView);
    }

    @FXML
    private void handleRoleTestChange() {
        Role selectedRole = comboRoleTest.getValue();
        if (selectedRole == null) {
            return;
        }

        SessionManager.setDevUser(selectedRole);
        updateSessionLabel();
        applyNavigationForCurrentRole();
        showDefaultViewForCurrentRole();
    }

    @FXML
    private void handleDyslexiaMode() {
        if (SessionManager.getCurrentUserRole() != Role.ETUDIANT) {
            btnDyslexiaMode.setSelected(false);
            setDyslexiaMode(false);
            return;
        }
        setDyslexiaMode(btnDyslexiaMode.isSelected());
    }

    private void setDyslexiaMode(boolean enabled) {
        if (enabled) {
            if (!appRoot.getStyleClass().contains("dyslexia-mode")) {
                appRoot.getStyleClass().add("dyslexia-mode");
            }
            btnDyslexiaMode.setText("Mode dyslexie active");
        } else {
            appRoot.getStyleClass().remove("dyslexia-mode");
            btnDyslexiaMode.setText("Mode dyslexie");
        }
    }

    private void updateSessionLabel() {
        lblSessionUser.setText(SessionManager.getCurrentUserName()
                + "\n" + SessionManager.getCurrentUserRoleLabel());
    }

    private void applyNavigationForCurrentRole() {
        Role role = SessionManager.getCurrentUserRole();
        boolean student = role == Role.ETUDIANT;

        accessibilityPanel.setVisible(student);
        accessibilityPanel.setManaged(student);
        if (!student) {
            btnDyslexiaMode.setSelected(false);
            setDyslexiaMode(false);
        }

        if (student) {
            btnNavEvenements.setVisible(false);
            btnNavEvenements.setManaged(false);
            btnNavReservations.setText("Mes reservations");
            return;
        }

        btnNavEvenements.setVisible(true);
        btnNavEvenements.setManaged(true);

        if (role == Role.INSTRUCTEUR) {
            btnNavEvenements.setText("Voir les evenements");
            btnNavReservations.setText("Reservations recues");
        } else {
            btnNavEvenements.setText("Gerer les evenements");
            btnNavReservations.setText("Gerer les reservations");
        }
    }

    private void showDefaultViewForCurrentRole() {
        if (SessionManager.getCurrentUserRole() == Role.ETUDIANT) {
            showReservations();
        } else {
            showEvenements();
        }
    }

    private boolean canOpenEvenementsPage() {
        return SessionManager.getCurrentUserRole() != Role.ETUDIANT;
    }

    private void setActiveButton(Button activeBtn) {
        btnNavEvenements.getStyleClass().remove("nav-btn-active");
        btnNavReservations.getStyleClass().remove("nav-btn-active");
        activeBtn.getStyleClass().add("nav-btn-active");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showAlert("Erreur interface", "Impossible de charger l'interface demandee.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
