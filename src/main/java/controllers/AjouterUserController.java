package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import services.ServiceUser;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterUserController {
    @javafx.fxml.FXML
    private TextField tfNomUtilisateur;
    @javafx.fxml.FXML
    private TextField tfEmail;
    @javafx.fxml.FXML
    private TextField tfMotDePasse;
    @javafx.fxml.FXML
    private TextField tfPrenom;
    @javafx.fxml.FXML
    private TextField tfNom;

    @javafx.fxml.FXML
    public void ajouter(ActionEvent actionEvent) {
        ServiceUser serviceUser = new ServiceUser();
        User user = new User(
            tfNomUtilisateur.getText(),
            tfEmail.getText(),
            tfMotDePasse.getText(),
            tfPrenom.getText(),
            tfNom.getText()
        );
        try {
            serviceUser.add(user);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("User ajoute");
            alert.show();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void allerVersList(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherUser.fxml"));
            tfNomUtilisateur.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}