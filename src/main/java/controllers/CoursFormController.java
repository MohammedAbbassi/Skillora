package controllers;

import entities.Cours;
import services.CoursService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;

public class CoursFormController {

    @FXML private Label formTitle;
    @FXML private TextField titreField;
    @FXML private MenuButton categorieMenu;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> dureeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea objectifArea;
    @FXML private TextField imageUrlField;
    @FXML private Button saveButton;

    private CoursService coursService = new CoursService();
    private Cours coursToEdit = null;
    private String[] availableCategories = {"Java", "Web", "IA", "Réseau", "Électronique"};

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        niveauCombo.setValue("DEBUTANT");

        dureeCombo.setItems(FXCollections.observableArrayList("1h", "2h", "3h", "5h", "10h", "20h+"));
        dureeCombo.setValue("2h");

        // Initialize categories
        for (String cat : availableCategories) {
            CheckMenuItem item = new CheckMenuItem(cat);
            categorieMenu.getItems().add(item);
        }
    }

    public void setCoursForEdit(Cours cours) {
        this.coursToEdit = cours;
        formTitle.setText("Modifier le cours : " + cours.getTitre());
        saveButton.setText("Mettre à jour");

        titreField.setText(cours.getTitre());
        
        // Set selected categories
        if (cours.getCategorie() != null) {
            String[] cats = cours.getCategorie().split(",\\s*");
            for (MenuItem item : categorieMenu.getItems()) {
                CheckMenuItem checkItem = (CheckMenuItem) item;
                checkItem.setSelected(false);
                for (String c : cats) {
                    if (checkItem.getText().equals(c)) {
                        checkItem.setSelected(true);
                        break;
                    }
                }
            }
        }

        niveauCombo.setValue(cours.getNiveau());
        dureeCombo.setValue(cours.getDuree());
        descriptionArea.setText(cours.getDescription());
        objectifArea.setText(cours.getObjectifSemaine());
        imageUrlField.setText(cours.getImageUrl());
    }

    @FXML
    void onCancel(ActionEvent event) {
        MainController.getInstance().loadViewIntoPage("/ui/cours-list.fxml", (VBox) formTitle.getScene().lookup("#pageCourses"));
    }

    @FXML
    void onSave(ActionEvent event) {
        try {
            // Validation
            if (isNullOrEmpty(titreField.getText()) || 
                isNullOrEmpty(descriptionArea.getText()) ||
                isNullOrEmpty(objectifArea.getText()) ||
                niveauCombo.getValue() == null ||
                dureeCombo.getValue() == null) {
                showAlert("Champs incomplets", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // Collect categories
            StringBuilder sb = new StringBuilder();
            int selectedCount = 0;
            for (MenuItem item : categorieMenu.getItems()) {
                CheckMenuItem checkItem = (CheckMenuItem) item;
                if (checkItem.isSelected()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(checkItem.getText());
                    selectedCount++;
                }
            }

            if (selectedCount == 0) {
                showAlert("Catégorie manquante", "Veuillez sélectionner au moins une catégorie.");
                return;
            }

            Cours c = (coursToEdit != null) ? coursToEdit : new Cours();
            c.setTitre(titreField.getText());
            c.setDescription(descriptionArea.getText());
            c.setCategorie(sb.toString());
            c.setNiveau(niveauCombo.getValue());
            c.setDuree(dureeCombo.getValue());
            c.setObjectifSemaine(objectifArea.getText());
            c.setImageUrl(imageUrlField.getText());
            
            if (coursToEdit == null) {
                c.setDateCreation(LocalDate.now());
                coursService.add(c);
            } else {
                coursService.update(c);
            }

            MainController.getInstance().loadViewIntoPage("/ui/cours-list.fxml", (VBox) formTitle.getScene().lookup("#pageCourses"));

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

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
