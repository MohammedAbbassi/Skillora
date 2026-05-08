package controllers;

import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceUser;

import java.sql.SQLException;
import java.util.List;

public class AfficherUserController {
    @FXML
    private TableColumn col_nomUtilisateur;
    @FXML
    private TableColumn col_email;
    @FXML
    private TableColumn col_prenom;
    @FXML
    private TableColumn col_nom;
    @FXML
    private TableView tv_users;

    @FXML
    void initialize() {
        ServiceUser serviceUser = new ServiceUser();
        try {
            List<User> userList = serviceUser.getAll();
            ObservableList<User> userObservableList = FXCollections.observableList(userList);
            tv_users.setItems(userObservableList);
            col_nomUtilisateur.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
            col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            col_prenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            col_nom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}