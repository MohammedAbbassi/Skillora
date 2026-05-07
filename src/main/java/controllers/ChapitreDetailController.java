package controllers;

import entities.Chapitre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ChapitreService;
import services.CoursService;
import services.CertificateService;
import java.sql.SQLException;
import java.util.List;

public class ChapitreDetailController {
    
    // ... fields ...
    private CertificateService certificateService = new CertificateService();


    @FXML private Label chapterTitleLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressTextLabel;
    @FXML private Label niveauLabel;
    @FXML private Label dureeLabel;
    @FXML private Label contenuLabel;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private ToggleButton btnComplete;
    @FXML private Button btnQuiz;


    @FXML private TextArea remarksArea;
    @FXML private ListView<String> tpFilesList;

    private Chapitre currentChapter;
    private List<Chapitre> contextChapters;
    private ChapitreService chapitreService = new ChapitreService();
    private CoursService coursService = new CoursService();


    public void setChapter(Chapitre ch, List<Chapitre> allChapters) {
        this.currentChapter = ch;
        this.contextChapters = allChapters;
        
        chapterTitleLabel.setText(ch.getTitre());
        niveauLabel.setText(ch.getNiveau() != null ? ch.getNiveau() : "MOYEN");
        niveauLabel.getStyleClass().removeAll("badge-facile", "badge-moyen", "badge-difficile");
        niveauLabel.getStyleClass().add("badge-" + niveauLabel.getText().toLowerCase());
        
        dureeLabel.setText(ch.getDuree() + " min");
        contenuLabel.setText(ch.getContenu());
        
        // Completion status
        btnComplete.setSelected(ch.isEstComplete());
        updateBtnCompleteStyle();

        // Load Remarks
        remarksArea.setText(ch.getRemarques() != null ? ch.getRemarques() : "");
        
        // Load TPs
        tpFilesList.getItems().clear();
        if (ch.getFichiersTp() != null && !ch.getFichiersTp().isEmpty()) {
            String[] files = ch.getFichiersTp().split(";");
            tpFilesList.getItems().addAll(files);
        }
        
        // Navigation state

        int index = contextChapters.indexOf(ch);
        btnPrev.setDisable(index <= 0);
        btnNext.setDisable(index >= contextChapters.size() - 1);
        
        // Progress (mock for now based on index)
        double progress = (double)(index + 1) / contextChapters.size();
        overallProgressBar.setProgress(progress);
        progressTextLabel.setText((int)(progress * 100) + "% du cours complété");
    }

    @FXML
    void onBack(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/chapitre-list.fxml");
    }

    @FXML
    void onPrev(ActionEvent event) {
        int index = contextChapters.indexOf(currentChapter);
        if (index > 0) setChapter(contextChapters.get(index - 1), contextChapters);
    }

    @FXML
    void onNext(ActionEvent event) {
        int index = contextChapters.indexOf(currentChapter);
        if (index < contextChapters.size() - 1) setChapter(contextChapters.get(index + 1), contextChapters);
    }

    @FXML
    void onToggleComplete(ActionEvent event) {
        try {
            boolean status = btnComplete.isSelected();
            currentChapter.setEstComplete(status);
            chapitreService.updateCompletionStatus(currentChapter.getIdChapitre(), status);
            
            // Re-calculate course progression
            coursService.calculateAndSaveProgression(currentChapter.getIdCours());
            
            updateBtnCompleteStyle();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBtnCompleteStyle() {
        boolean completed = btnComplete.isSelected();
        if (completed) {
            btnComplete.setText("✓ Terminé");
            btnComplete.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-border-color: #10b981;");
        } else {
            btnComplete.setText("Terminer ce chapitre");
            btnComplete.setStyle("");
        }
        
        if (btnQuiz != null) {
            btnQuiz.setVisible(completed);
            btnQuiz.setManaged(completed);
        }
    }

    @FXML
    void onQuizClick(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/quiz-view.fxml");
    }


    @FXML 
    void onDownloadChapter(ActionEvent event) {

        try {
            String path = certificateService.generateChapterPDF(currentChapter);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Le chapitre a été téléchargé avec succès : " + path);
            alert.show();
            
            // Open PDF
            java.io.File file = new java.io.File(path);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void onReadPdf(ActionEvent event) { /* Open PDF from DB if exists */ }

    @FXML void onGenerateSummary(ActionEvent event) { /* AI Summary mock */ }

    @FXML
    void onSaveUserData(ActionEvent event) {
        try {
            currentChapter.setRemarques(remarksArea.getText());
            StringBuilder sb = new StringBuilder();
            for (String f : tpFilesList.getItems()) {
                if (sb.length() > 0) sb.append(";");
                sb.append(f);
            }
            currentChapter.setFichiersTp(sb.toString());
            
            chapitreService.updateUserData(currentChapter.getIdChapitre(), currentChapter.getRemarques(), currentChapter.getFichiersTp());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Vos remarques et fichiers ont été enregistrés.");
            alert.show();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void onAddTp(ActionEvent event) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Ajouter un rendu (PDF)");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        java.io.File file = fc.showOpenDialog(remarksArea.getScene().getWindow());
        if (file != null) {
            tpFilesList.getItems().add(file.getAbsolutePath());
        }
    }

    @FXML
    void onOpenTp(ActionEvent event) {
        String selected = tpFilesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                java.io.File file = new java.io.File(selected);
                if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

}
