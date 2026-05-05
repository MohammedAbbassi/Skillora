package controllers;

import entities.Cours;
import services.CoursService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class CoursFormController {

    @FXML private Label formTitle;
    @FXML private TextField titreField;
    @FXML private TextField categorieField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> dureeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea objectifArea;
    @FXML private TextArea performanceArea;
    @FXML private Slider progressionSlider;
    @FXML private Label progressionLabel;
    @FXML private Button saveButton;

    private CoursService coursService = new CoursService();
    private Cours coursToEdit = null;

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        niveauCombo.setValue("DEBUTANT");

        dureeCombo.setItems(FXCollections.observableArrayList("1h", "2h", "3h", "5h", "10h", "20h+"));
        dureeCombo.setValue("2h");

        // Bind slider value to label
        progressionSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            progressionLabel.setText(newVal.intValue() + "%");
        });
    }

    public void setCoursForEdit(Cours cours) {
        this.coursToEdit = cours;
        formTitle.setText("Modifier le cours : " + cours.getTitre());
        saveButton.setText("Mettre à jour");

        titreField.setText(cours.getTitre());
        categorieField.setText(cours.getCategorie());
        niveauCombo.setValue(cours.getNiveau());
        dureeCombo.setValue(cours.getDuree());
        descriptionArea.setText(cours.getDescription());
        objectifArea.setText(cours.getObjectifSemaine());
        performanceArea.setText(cours.getPerformance());
        progressionSlider.setValue(cours.getProgression());
    }

    @FXML
    void onCancel(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/cours-list.fxml");
    }

    @FXML
    void onSave(ActionEvent event) {
        try {
            if (titreField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le titre est obligatoire.");
                return;
            }

            Cours c = (coursToEdit != null) ? coursToEdit : new Cours();
            c.setTitre(titreField.getText());
            c.setDescription(descriptionArea.getText());
            c.setCategorie(categorieField.getText());
            c.setNiveau(niveauCombo.getValue());
            c.setDuree(dureeCombo.getValue());
            c.setObjectifSemaine(objectifArea.getText());
            c.setPerformance(performanceArea.getText());
            c.setProgression((int) progressionSlider.getValue());
            
            if (coursToEdit == null) {
                c.setDateCreation(LocalDate.now());
                coursService.add(c);
            } else {
                coursService.update(c);
            }

            MainLayoutController.getInstance().loadView("/ui/cours-list.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur BD", "Impossible d'enregistrer le cours.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
