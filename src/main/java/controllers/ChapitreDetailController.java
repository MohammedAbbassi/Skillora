package controllers;

import entities.Chapitre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ChapitreService;
import services.CoursService;
import services.CertificateService;
import services.DictionaryService;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Platform;

public class ChapitreDetailController {
    
    private CertificateService certificateService = new CertificateService();
    private ChapitreService chapitreService = new ChapitreService();
    private CoursService coursService = new CoursService();
    private DictionaryService dictionaryService = new DictionaryService();

    @FXML private Label chapterTitleLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressTextLabel;
    @FXML private Label niveauLabel;
    @FXML private Label dureeLabel;
    @FXML private TextArea contenuArea;
    @FXML private Button btnPrev;

    @FXML private Button btnNext;
    @FXML private ToggleButton btnComplete;
    @FXML private Button btnQuiz;
    @FXML private Button btnYoutube;

    @FXML private TextField wordSearchField;
    @FXML private Label definitionLabel;

    @FXML private TextArea remarksArea;

    @FXML private ListView<String> tpFilesList;

    private Chapitre currentChapter;
    private List<Chapitre> contextChapters;

    @FXML
    public void initialize() {
        if (remarksArea != null) {
            utils.InputValidator.applyLettersOnly(remarksArea);
        }
        // L'utilisateur normal ne peut pas modifier, seulement copier
        if (contenuArea != null) {
            contenuArea.setEditable(MainLayoutController.getInstance().isAdminMode());
        }
    }


    public void setChapter(Chapitre ch, List<Chapitre> allChapters) {
        this.currentChapter = ch;
        this.contextChapters = allChapters;
        
        chapterTitleLabel.setText(ch.getTitre());
        niveauLabel.setText(ch.getNiveau() != null ? ch.getNiveau() : "MOYEN");
        niveauLabel.getStyleClass().removeAll("badge-facile", "badge-moyen", "badge-difficile");
        niveauLabel.getStyleClass().add("badge-" + niveauLabel.getText().toLowerCase());
        
        dureeLabel.setText(ch.getDuree() + " min");
        contenuArea.setText(ch.getContenu());

        
        // Handle YouTube Button visibility
        if (btnYoutube != null) {
            boolean hasYoutube = ch.getYoutubeLink() != null && !ch.getYoutubeLink().trim().isEmpty();
            btnYoutube.setVisible(hasYoutube);
            btnYoutube.setManaged(hasYoutube);
        }

        btnComplete.setSelected(ch.isEstComplete());
        updateBtnCompleteStyle();

        remarksArea.setText(ch.getRemarques() != null ? ch.getRemarques() : "");
        
        tpFilesList.getItems().clear();
        if (ch.getFichiersTp() != null && !ch.getFichiersTp().isEmpty()) {
            String[] files = ch.getFichiersTp().split(";");
            tpFilesList.getItems().addAll(files);
        }
        
        int index = (contextChapters != null) ? contextChapters.indexOf(ch) : -1;
        if (index != -1) {
            btnPrev.setDisable(index <= 0);
            btnNext.setDisable(index >= contextChapters.size() - 1);
            
            double progress = (double)(index + 1) / contextChapters.size();
            overallProgressBar.setProgress(progress);
            progressTextLabel.setText((int)(progress * 100) + "% du cours complété");
        }
    }

    @FXML
    void onBack(ActionEvent event) {
        try {
            entities.Cours cours = coursService.getById(currentChapter.getIdCours());
            ChapitreListController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/chapitre-list.fxml");
            if (ctrl != null && cours != null) {
                ctrl.setCourseContext(cours);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainLayoutController.getInstance().loadView("/ui/chapitre-list.fxml");
        }
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
        QuizController ctrl = MainLayoutController.getInstance().loadViewAndGetController("/ui/quiz-view.fxml");
        if (ctrl != null) {
            ctrl.setChapter(currentChapter);
        }
    }


    @FXML 
    void onDownloadChapter(ActionEvent event) {
        try {
            String path = certificateService.generateChapterPDF(currentChapter);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Le chapitre a été téléchargé avec succès : " + path);
            alert.show();
            java.io.File file = new java.io.File(path);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML 
    void onReadPdf(ActionEvent event) {
        if (currentChapter != null && currentChapter.getPdfUrl() != null && !currentChapter.getPdfUrl().isEmpty()) {
            try {
                java.io.File file = new java.io.File(currentChapter.getPdfUrl());
                if (file.exists()) {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "L'ouverture de fichiers n'est pas supportée sur ce système.");
                    }
                } else {
                    showAlert("Fichier introuvable", "Le fichier PDF n'existe pas au chemin spécifié : \n" + currentChapter.getPdfUrl());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir le fichier : " + e.getMessage());
            }
        } else {
            showAlert("Aucun support", "Ce chapitre ne possède pas de support PDF.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onOpenYoutube(ActionEvent event) {
        if (currentChapter != null && currentChapter.getYoutubeLink() != null) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(currentChapter.getYoutubeLink()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onSearchDefinition(ActionEvent event) {
        String word = wordSearchField.getText();
        if (word == null || word.isEmpty()) return;
        definitionLabel.setText("Recherche (EN) en cours...");
        dictionaryService.getEnglishDefinition(word).thenAccept(definition -> {
            Platform.runLater(() -> definitionLabel.setText(definition));
        }).exceptionally(ex -> {
            Platform.runLater(() -> definitionLabel.setText("Erreur : Dictionnaire EN injoignable."));
            return null;
        });
    }

    @FXML
    void onSearchFrenchDefinition(ActionEvent event) {
        String word = wordSearchField.getText();
        if (word == null || word.isEmpty()) return;
        definitionLabel.setText("Recherche (FR) en cours...");
        dictionaryService.getFrenchDefinition(word).thenAccept(definition -> {
            Platform.runLater(() -> definitionLabel.setText(definition));
        }).exceptionally(ex -> {
            Platform.runLater(() -> definitionLabel.setText("Erreur : Wiktionary FR injoignable."));
            return null;
        });
    }


    @FXML
    void onSearchWiktApi(ActionEvent event) {
        String word = wordSearchField.getText();
        if (word == null || word.isEmpty()) return;
        definitionLabel.setText("Recherche (WiktAPI) en cours...");
        dictionaryService.getWiktApiDefinition(word).thenAccept(definition -> {
            Platform.runLater(() -> definitionLabel.setText(definition));
        }).exceptionally(ex -> {
            Platform.runLater(() -> definitionLabel.setText("Erreur : WiktAPI injoignable."));
            return null;
        });
    }

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
