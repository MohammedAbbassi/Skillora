package controllers;

import entities.Poste;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import services.PosteService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterPosteController {
    @FXML private TextField tfTitre;
    @FXML private TextField tfContenu;

    private PosteService posteService = new PosteService();

    @FXML
    public void ajouter(ActionEvent actionEvent) {
        Poste poste = new Poste(tfTitre.getText(), tfContenu.getText(), "", 1);
        try {
            this.posteService.add(poste);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void allerVersList(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherPoste.fxml"));
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
