package controllers;

import entities.Poste;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.PosteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherPosteController {
    @FXML private TableView<Poste> tableView;
    @FXML private TableColumn<Poste, String> colTitre;
    @FXML private TableColumn<Poste, String> colContenu;

    private PosteService posteService = new PosteService();

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        refreshTable();
    }

    private void refreshTable() {
        try {
            List<Poste> posts = posteService.getAll();
            ObservableList<Poste> observableList = FXCollections.observableArrayList(posts);
            tableView.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void allerVersAjout(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterPoste.fxml"));
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
