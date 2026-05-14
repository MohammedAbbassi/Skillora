package controllers;

import entities.Chapitre;
import entities.Cours;
import services.ChapitreService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ChapitreAdminController {

    @FXML private Label formTitle;
    @FXML private Label courseContextLabel;
    @FXML private TextField titreField;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField youtubeLinkField;
    @FXML private TextField pdfUrlField;
    @FXML private TextField imageUrlField;
    @FXML private TextArea contenuArea;
    @FXML private Button btnDelete;
    @FXML private Button saveButton;

    private ChapitreService chapitreService = new ChapitreService();
    private Cours currentCourse;
    private Chapitre chapterToEdit;

    @FXML
    public void initialize() {
        if (niveauCombo != null) {
            niveauCombo.setItems(FXCollections.observableArrayList("FACILE", "MOYEN", "DIFFICILE"));
            niveauCombo.setValue("MOYEN");
        }
        if (typeCombo != null) {
            typeCombo.setItems(FXCollections.observableArrayList("TEXTE", "VIDEO"));
            typeCombo.setValue("VIDEO");
        }

        // Input Validation
        if (titreField != null) utils.InputValidator.applyLettersOnly(titreField);
        if (contenuArea != null) utils.InputValidator.applyLettersOnly(contenuArea);
    }

    public void setCourseContext(Cours cours) {
        this.currentCourse = cours;
        courseContextLabel.setText("Cours : " + cours.getTitre());
    }

    public void setChapterForEdit(Chapitre ch, Cours cours) {
        this.chapterToEdit = ch;
        this.currentCourse = cours;
        
        formTitle.setText("Modifier le Chapitre");
        courseContextLabel.setText("Cours : " + cours.getTitre());
        saveButton.setText("Mettre à jour");
        btnDelete.setVisible(true);

        titreField.setText(ch.getTitre());
        dureeField.setText(String.valueOf(ch.getDuree()));
        if (niveauCombo != null) niveauCombo.setValue(ch.getNiveau());
        if (typeCombo != null) typeCombo.setValue(ch.getTypeExplication());
        youtubeLinkField.setText(ch.getYoutubeLink());
        pdfUrlField.setText(ch.getPdfUrl());
        imageUrlField.setText(ch.getImageUrl());
        contenuArea.setText(ch.getContenu());
    }

    @FXML
    void onCancel(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/chapitre-list.fxml");
    }

    @FXML
    void onDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setContentText("Voulez-vous vraiment supprimer ce chapitre ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                chapitreService.delete(chapterToEdit);
                onCancel(null);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    void onOpenYoutube(ActionEvent event) {
        String url = youtubeLinkField.getText();
        if (url != null && !url.trim().isEmpty()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'ouvrir le lien YouTube.");
            }
        }
    }

    @FXML
    void onBrowsePdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le support PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(pdfUrlField.getScene().getWindow());
        
        if (selectedFile != null) {
            pdfUrlField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void onSave(ActionEvent event) {
        try {
            // Validation
            if (isNullOrEmpty(titreField.getText()) ||
                isNullOrEmpty(dureeField.getText()) ||
                isNullOrEmpty(contenuArea.getText()) ||
                (typeCombo != null && typeCombo.getValue() == null)) {
                showAlert("Champs incomplets", "Veuillez remplir tous les champs obligatoires du chapitre.");
                return;
            }

            if (currentCourse == null) {
                showAlert("Erreur", "Aucun cours n'est sélectionné pour ce chapitre.");
                return;
            }

            Chapitre ch = (chapterToEdit != null) ? chapterToEdit : new Chapitre();
            ch.setTitre(titreField.getText());
            ch.setContenu(contenuArea.getText());
            if (niveauCombo != null) ch.setNiveau(niveauCombo.getValue());
            if (typeCombo != null) ch.setTypeExplication(typeCombo.getValue());
            ch.setYoutubeLink(youtubeLinkField.getText());
            ch.setPdfUrl(pdfUrlField.getText());
            ch.setImageUrl(imageUrlField.getText());
            ch.setIdCours(currentCourse.getIdCours());

            try { ch.setDuree(Integer.parseInt(dureeField.getText())); } catch (Exception e) { ch.setDuree(0); }

            if (chapterToEdit == null) chapitreService.add(ch);
            else chapitreService.update(ch);

            List<Chapitre> allChapters = chapitreService.getByCours(currentCourse.getIdCours());
            ChapitreDetailController controller = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-detail.fxml");
            if (controller != null) {
                controller.setChapter(ch, allChapters);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de création", "Détail de l'erreur : " + e.getMessage());
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
