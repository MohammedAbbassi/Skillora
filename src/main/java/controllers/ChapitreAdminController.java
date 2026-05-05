package controllers;

import entities.Chapitre;
import entities.Cours;
import services.ChapitreService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.util.Optional;

public class ChapitreAdminController {

    @FXML private Label formTitle;
    @FXML private Label courseContextLabel;
    @FXML private TextField titreField;
    @FXML private TextField ordreField;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField videoUrlField;
    @FXML private TextField pdfUrlField;
    @FXML private TextField imageUrlField;
    @FXML private TextArea resumeArea;
    @FXML private TextArea contenuArea;
    @FXML private Button btnDelete;
    @FXML private Button saveButton;

    private ChapitreService chapitreService = new ChapitreService();
    private Cours currentCourse;
    private Chapitre chapterToEdit;

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("FACILE", "MOYEN", "DIFFICILE"));
        niveauCombo.setValue("MOYEN");
        typeCombo.setItems(FXCollections.observableArrayList("TEXTE", "VIDEO"));
        typeCombo.setValue("VIDEO");
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
        ordreField.setText(String.valueOf(ch.getOrdre()));
        dureeField.setText(String.valueOf(ch.getDuree()));
        niveauCombo.setValue(ch.getNiveau());
        typeCombo.setValue(ch.getTypeExplication());
        videoUrlField.setText(ch.getVideoUrl());
        pdfUrlField.setText(ch.getPdfUrl());
        imageUrlField.setText(ch.getImageUrl());
        resumeArea.setText(ch.getResume());
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
    void onSave(ActionEvent event) {
        try {
            if (titreField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le titre est obligatoire.");
                return;
            }

            Chapitre ch = (chapterToEdit != null) ? chapterToEdit : new Chapitre();
            ch.setTitre(titreField.getText());
            ch.setContenu(contenuArea.getText());
            ch.setResume(resumeArea.getText());
            ch.setNiveau(niveauCombo.getValue());
            ch.setTypeExplication(typeCombo.getValue());
            ch.setVideoUrl(videoUrlField.getText());
            ch.setPdfUrl(pdfUrlField.getText());
            ch.setImageUrl(imageUrlField.getText());
            ch.setIdCours(currentCourse.getIdCours());

            try { ch.setOrdre(Integer.parseInt(ordreField.getText())); } catch (Exception e) { ch.setOrdre(1); }
            try { ch.setDuree(Integer.parseInt(dureeField.getText())); } catch (Exception e) { ch.setDuree(0); }

            if (chapterToEdit == null) chapitreService.add(ch);
            else chapitreService.update(ch);

            onCancel(null);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
