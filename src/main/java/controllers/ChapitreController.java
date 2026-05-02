package controllers;

import entities.Chapitre;
import entities.Cours;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import services.ChapitreService;
import services.CoursService;

import java.sql.SQLException;
import java.util.List;

public class ChapitreController {

    @FXML private TableView<Chapitre> tableChapitres;
    @FXML private TableColumn<Chapitre, String> colTitre;
    @FXML private TableColumn<Chapitre, Integer> colOrdre;
    @FXML private TableColumn<Chapitre, Integer> colDuree;
    @FXML private TableColumn<Chapitre, String> colType;
    @FXML private TableColumn<Chapitre, Integer> colCours;

    @FXML private TextField txtTitre;
    @FXML private TextArea txtContenu;
    @FXML private TextField txtOrdre;
    @FXML private TextField txtDuree;
    @FXML private TextField txtPdfUrl;
    @FXML private ComboBox<Cours> comboCours;
    @FXML private TextArea txtResume;
    @FXML private ComboBox<String> comboType;

    private final ChapitreService chapitreService = new ChapitreService();
    private final CoursService coursService = new CoursService();
    private ObservableList<Chapitre> listeChapitres = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeExplication"));
        colCours.setCellValueFactory(new PropertyValueFactory<>("idCours"));

        comboType.setItems(FXCollections.observableArrayList("TEXTE", "VIDEO"));

        setupComboCours();
        refreshTable();

        tableChapitres.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtTitre.setText(newSelection.getTitre());
                txtContenu.setText(newSelection.getContenu());
                txtOrdre.setText(String.valueOf(newSelection.getOrdre()));
                txtDuree.setText(String.valueOf(newSelection.getDuree()));
                txtPdfUrl.setText(newSelection.getPdfUrl());
                txtResume.setText(newSelection.getResume());
                comboType.setValue(newSelection.getTypeExplication());

                // Select the correct cours in combo
                for (Cours c : comboCours.getItems()) {
                    if (c.getIdCours() == newSelection.getIdCours()) {
                        comboCours.setValue(c);
                        break;
                    }
                }
            }
        });
    }

    private void setupComboCours() {
        try {
            List<Cours> coursList = coursService.getAll();
            comboCours.setItems(FXCollections.observableArrayList(coursList));
            comboCours.setConverter(new StringConverter<Cours>() {
                @Override
                public String toString(Cours cours) {
                    return cours == null ? "" : cours.getTitre();
                }

                @Override
                public Cours fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Chapitre> chapitres = chapitreService.getAll();
            listeChapitres.setAll(chapitres);
            tableChapitres.setItems(listeChapitres);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les chapitres: " + e.getMessage());
        }
    }

    @FXML
    public void ajouterChapitre() {
        try {
            if (comboCours.getValue() == null) {
                showAlert("Attention", "Veuillez sélectionner un cours.");
                return;
            }

            Chapitre ch = new Chapitre();
            ch.setTitre(txtTitre.getText());
            ch.setContenu(txtContenu.getText());
            ch.setOrdre(Integer.parseInt(txtOrdre.getText()));
            ch.setDuree(Integer.parseInt(txtDuree.getText()));
            ch.setPdfUrl(txtPdfUrl.getText());
            ch.setIdCours(comboCours.getValue().getIdCours());
            ch.setResume(txtResume.getText());
            ch.setTypeExplication(comboType.getValue());

            chapitreService.add(ch);
            refreshTable();
            viderChamps();
            showAlert("Succès", "Chapitre ajouté avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifierChapitre() {
        Chapitre selected = tableChapitres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un chapitre à modifier.");
            return;
        }

        try {
            selected.setTitre(txtTitre.getText());
            selected.setContenu(txtContenu.getText());
            selected.setOrdre(Integer.parseInt(txtOrdre.getText()));
            selected.setDuree(Integer.parseInt(txtDuree.getText()));
            selected.setPdfUrl(txtPdfUrl.getText());
            selected.setIdCours(comboCours.getValue().getIdCours());
            selected.setResume(txtResume.getText());
            selected.setTypeExplication(comboType.getValue());

            chapitreService.update(selected);
            refreshTable();
            showAlert("Succès", "Chapitre modifié avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerChapitre() {
        Chapitre selected = tableChapitres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un chapitre à supprimer.");
            return;
        }

        try {
            chapitreService.delete(selected);
            refreshTable();
            viderChamps();
            showAlert("Succès", "Chapitre supprimé !");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @FXML
    public void viderChamps() {
        txtTitre.clear();
        txtContenu.clear();
        txtOrdre.clear();
        txtDuree.clear();
        txtPdfUrl.clear();
        comboCours.setValue(null);
        txtResume.clear();
        comboType.setValue(null);
        tableChapitres.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
