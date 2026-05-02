package controllers;

import entities.Cours;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.CoursService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CoursController {

    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, String> colTitre;
    @FXML private TableColumn<Cours, String> colDescription;
    @FXML private TableColumn<Cours, String> colDomaine;
    @FXML private TableColumn<Cours, String> colNiveau;
    @FXML private TableColumn<Cours, Integer> colDuree;
    @FXML private TableColumn<Cours, LocalDate> colDateCreation;
    @FXML private TableColumn<Cours, Integer> colProgression;

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtDomaine;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private TextField txtDuree;
    @FXML private DatePicker dateCreation;
    @FXML private TextField txtProgression;

    private final CoursService coursService = new CoursService();
    private ObservableList<Cours> listeCours = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDomaine.setCellValueFactory(new PropertyValueFactory<>("domaine"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colProgression.setCellValueFactory(new PropertyValueFactory<>("progression"));

        comboNiveau.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));

        refreshTable();

        tableCours.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtTitre.setText(newSelection.getTitre());
                txtDescription.setText(newSelection.getDescription());
                txtDomaine.setText(newSelection.getDomaine());
                comboNiveau.setValue(newSelection.getNiveau());
                txtDuree.setText(String.valueOf(newSelection.getDuree()));
                dateCreation.setValue(newSelection.getDateCreation());
                txtProgression.setText(String.valueOf(newSelection.getProgression()));
            }
        });
    }

    private void refreshTable() {
        try {
            List<Cours> cours = coursService.getAll();
            listeCours.setAll(cours);
            tableCours.setItems(listeCours);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage());
        }
    }

    @FXML
    public void ajouterCours() {
        try {
            Cours c = new Cours();
            c.setTitre(txtTitre.getText());
            c.setDescription(txtDescription.getText());
            c.setDomaine(txtDomaine.getText());
            c.setNiveau(comboNiveau.getValue());
            c.setDuree(Integer.parseInt(txtDuree.getText()));
            c.setDateCreation(dateCreation.getValue());
            c.setProgression(Integer.parseInt(txtProgression.getText()));

            coursService.add(c);
            refreshTable();
            viderChamps();
            showAlert("Succès", "Cours ajouté avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifierCours() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un cours à modifier.");
            return;
        }

        try {
            selected.setTitre(txtTitre.getText());
            selected.setDescription(txtDescription.getText());
            selected.setDomaine(txtDomaine.getText());
            selected.setNiveau(comboNiveau.getValue());
            selected.setDuree(Integer.parseInt(txtDuree.getText()));
            selected.setDateCreation(dateCreation.getValue());
            selected.setProgression(Integer.parseInt(txtProgression.getText()));

            coursService.update(selected);
            refreshTable();
            showAlert("Succès", "Cours modifié avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerCours() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un cours à supprimer.");
            return;
        }

        try {
            coursService.delete(selected);
            refreshTable();
            viderChamps();
            showAlert("Succès", "Cours supprimé !");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @FXML
    public void viderChamps() {
        txtTitre.clear();
        txtDescription.clear();
        txtDomaine.clear();
        comboNiveau.setValue(null);
        txtDuree.clear();
        dateCreation.setValue(null);
        txtProgression.clear();
        tableCours.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
