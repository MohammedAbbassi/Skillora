package com.skillora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
    private Label lblSessionUser;

    private String currentView = "/com/skillora/views/EvenementInterface.fxml";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mettreAJourInfosSession();
        adapterNavigationSelonRole();
        afficherPageParDefautSelonRole();
    }

    @FXML
    private void afficherPageEvenements() {
        if (!peutOuvrirPageEvenements()) {
            afficherPageReservations();
            return;
        }
        marquerBoutonActif(btnNavEvenements);
        currentView = "/com/skillora/views/EvenementInterface.fxml";
        chargerVue(currentView);
    }

    @FXML
    private void afficherPageReservations() {
        marquerBoutonActif(btnNavReservations);
        currentView = "/com/skillora/views/ReservationInterface.fxml";
        chargerVue(currentView);
    }

    private void mettreAJourInfosSession() {
        lblSessionUser.setText(SessionManager.getCurrentUserName()
                + "\n" + SessionManager.getCurrentUserRoleLabel());
    }

    private void adapterNavigationSelonRole() {
        Role role = SessionManager.getCurrentUserRole();
        boolean student = role == Role.ETUDIANT;

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

    private void afficherPageParDefautSelonRole() {
        if (SessionManager.getCurrentUserRole() == Role.ETUDIANT) {
            afficherPageReservations();
        } else {
            afficherPageEvenements();
        }
    }

    private boolean peutOuvrirPageEvenements() {
        return SessionManager.getCurrentUserRole() != Role.ETUDIANT;
    }

    private void marquerBoutonActif(Button activeBtn) {
        btnNavEvenements.getStyleClass().remove("nav-btn-active");
        btnNavReservations.getStyleClass().remove("nav-btn-active");
        activeBtn.getStyleClass().add("nav-btn-active");
    }

    private void chargerVue(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            afficherAlerte("Erreur interface", "Impossible de charger l'interface demandee.");
        }
    }

    private void afficherAlerte(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
