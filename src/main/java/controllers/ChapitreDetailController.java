package controllers;

import entities.Chapitre;
import entities.UserPreference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import services.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChapitreDetailController {
    
    private CertificateService certificateService = new CertificateService();
    private ChapitreService chapitreService = new ChapitreService();
    private CoursService coursService = new CoursService();
    private final DyslexiaService dyslexiaService = new DyslexiaService();
    private final DictionaryService dictionaryService = new DictionaryService();
    private final SummarizerService summarizerService = SummarizerService.getInstance();
    private final TTSService ttsService = new TTSService();


    @FXML private Label chapterTitleLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressTextLabel;
    @FXML private Label niveauLabel;
    @FXML private Label dureeLabel;
    @FXML private TextArea contenuArea;
    @FXML private ImageView chapterBannerImage;
    @FXML private Button btnPrev;

    @FXML private Button btnNext;
    @FXML private ToggleButton btnComplete;
    @FXML private Button btnQuiz;
    @FXML private Button btnYoutube;

    @FXML private TextArea remarksArea;

    @FXML private ListView<String> tpFilesList;

    @FXML private StackPane contentStack;
    @FXML private ScrollPane dyslexiaScrollPane;
    @FXML private TextFlow dyslexiaTextFlow;
    @FXML private HBox dyslexiaPanelContainer;

    // Dictionary UI
    @FXML private VBox defEmptyState;
    @FXML private VBox defFoundState;
    @FXML private Label defWordTitle;
    @FXML private Label defPhonetic;
    @FXML private VBox definitionsContainer;
    @FXML private FlowPane synonymsContainer;
    @FXML private Label synonymsLabel;
    @FXML private Button btnLangFr;
    @FXML private Button btnLangEn;
    @FXML private TextField dictSearchField;

    // Supervisor Bar
    @FXML private ToggleButton btnPresence;
    @FXML private Label currentTimeLabel;
    @FXML private Label sessionTimerLabel;
    
    // Writing Assistant Tools
    @FXML private Label spellCheckStatus;
    @FXML private TextField calcDisplay;
    @FXML private Button btnAutoCorrect;
    @FXML private Label summaryLabel;
    
    private Map<String, String> commonCorrections = new HashMap<>();
    
    private double firstOperand = 0;
    private String currentOperator = "";
    private boolean isNewNumber = true;

    private Timeline clockTimeline;
    private int sessionSeconds = 0;
    
    private DyslexiaSettingsController settingsController;
    private UserPreference userPref;

    private Chapitre currentChapter;
    private List<Chapitre> contextChapters;

    @FXML
    public void initialize() {
        if (remarksArea != null) {
            utils.InputValidator.applyLettersOnly(remarksArea);
        }
        
        // Support dictionnaire pour le TextArea standard (Sélection de texte)
        contenuArea.selectedTextProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                showWordDefinition(newVal.trim());
            }
        });

        loadDyslexiaSettings();
        startSupervisorBar();
    }

    private void startSupervisorBar() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // Update Clock
            currentTimeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            // Update Session Timer
            sessionSeconds++;
            int mins = sessionSeconds / 60;
            int secs = sessionSeconds % 60;
            sessionTimerLabel.setText(String.format("%02d:%02d", mins, secs));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    @FXML
    void onTogglePresence(ActionEvent event) {
        if (btnPresence.isSelected()) {
            btnPresence.setText("✓ Présent");
            btnPresence.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15;");
        } else {
            btnPresence.setText("Marquer ma présence");
            btnPresence.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15;");
        }
    }

    // --- Writing Assistant: Spell Checker ---
    @FXML
    void onCheckSpelling(ActionEvent event) {
        String text = remarksArea.getText();
        if (text == null || text.isEmpty()) {
            spellCheckStatus.setText("Écrivez quelque chose d'abord.");
            btnAutoCorrect.setVisible(false);
            return;
        }
        
        if (commonCorrections.isEmpty()) {
            commonCorrections.put("javaa", "Java");
            commonCorrections.put("programmationn", "programmation");
            commonCorrections.put("skilloraa", "Skillora");
            commonCorrections.put("chapitree", "chapitre");
            commonCorrections.put("professeurr", "professeur");
            commonCorrections.put("etudiantt", "étudiant");
        }

        boolean found = false;
        String detectedError = "";
        for (String err : commonCorrections.keySet()) {
            if (text.toLowerCase().contains(err)) {
                detectedError = err;
                found = true;
                break;
            }
        }
        
        if (found) {
            spellCheckStatus.setText("⚠️ Erreur : '" + detectedError + "' détectée.");
            spellCheckStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            btnAutoCorrect.setVisible(true);
            btnAutoCorrect.setManaged(true);
        } else {
            spellCheckStatus.setText("✓ Texte impeccable !");
            spellCheckStatus.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
            btnAutoCorrect.setVisible(false);
            btnAutoCorrect.setManaged(false);
        }
    }

    @FXML
    void onAutoCorrect(ActionEvent event) {
        String text = remarksArea.getText();
        for (Map.Entry<String, String> entry : commonCorrections.entrySet()) {
            text = text.replaceAll("(?i)" + entry.getKey(), entry.getValue());
        }
        remarksArea.setText(text);
        spellCheckStatus.setText("✓ Correction appliquée !");
        spellCheckStatus.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px;");
        btnAutoCorrect.setVisible(false);
        btnAutoCorrect.setManaged(false);
    }

    @FXML
    void onGenerateSummary(ActionEvent event) {
        if (currentChapter == null) return;
        
        summaryLabel.setText("🤖 Analyse en cours...");
        summaryLabel.setStyle("-fx-text-fill: #6366f1; -fx-padding: 10; -fx-background-color: #f5f3ff; -fx-background-radius: 8;");
        
        summarizerService.summarize(currentChapter.getContenu()).thenAccept(summary -> {
            javafx.application.Platform.runLater(() -> {
                summaryLabel.setText(summary);
                summaryLabel.setStyle("-fx-text-fill: #334155; -fx-padding: 10; -fx-background-color: #f0f9ff; -fx-background-radius: 8; -fx-border-color: #bae6fd; -fx-border-radius: 8;");
            });
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> {
                summaryLabel.setText("❌ Erreur lors du résumé.");
            });
            return null;
        });
    }

    // --- Writing Assistant: Calculator ---
    @FXML
    void onCalcDigit(ActionEvent event) {
        String digit = ((Button)event.getSource()).getText();
        if (isNewNumber) {
            calcDisplay.setText(digit);
            isNewNumber = false;
        } else {
            calcDisplay.setText(calcDisplay.getText() + digit);
        }
    }

    @FXML
    void onCalcOp(ActionEvent event) {
        try {
            firstOperand = Double.parseDouble(calcDisplay.getText());
            currentOperator = ((Button)event.getSource()).getText();
            isNewNumber = true;
        } catch (NumberFormatException e) {
            calcDisplay.setText("Error");
        }
    }

    @FXML
    void onCalcClear(ActionEvent event) {
        calcDisplay.setText("0");
        firstOperand = 0;
        currentOperator = "";
        isNewNumber = true;
    }

    @FXML
    void onCalcEquals(ActionEvent event) {
        try {
            double secondOperand = Double.parseDouble(calcDisplay.getText());
            double result = 0;
            switch (currentOperator) {
                case "+": result = firstOperand + secondOperand; break;
                case "-": result = firstOperand - secondOperand; break;
                case "*": result = firstOperand * secondOperand; break;
                case "/": result = secondOperand != 0 ? firstOperand / secondOperand : 0; break;
                default: result = secondOperand;
            }
            calcDisplay.setText(String.valueOf(result));
            isNewNumber = true;
        } catch (NumberFormatException e) {
            calcDisplay.setText("Error");
        }
    }

    private void loadDyslexiaSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dyslexia-settings.fxml"));
            VBox panel = loader.load();
            settingsController = loader.getController();
            dyslexiaPanelContainer.getChildren().add(panel);

            long userId = (SessionService.getInstance().getCurrentUser() != null) 
                          ? SessionService.getInstance().getCurrentUser().getIdUtilisateur() : 1;
            
            settingsController.loadPreferences(userId);
            settingsController.setOnUpdate(this::applyDyslexiaSettings);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyDyslexiaSettings(UserPreference pref) {
        this.userPref = pref;
        boolean modeActive = settingsController.isModeActive();
        
        contenuArea.setVisible(!modeActive);
        dyslexiaScrollPane.setVisible(modeActive);
        
        if (modeActive) {
            renderDyslexiaText();
            // Background apply to the container and the flow
            String bgStyle = "-fx-background-color: " + pref.getCouleurFond() + ";";
            dyslexiaScrollPane.setStyle(bgStyle + "-fx-border-color: #cbd5e1; -fx-border-radius: 15;");
            dyslexiaTextFlow.setStyle(bgStyle + "-fx-line-spacing: " + (pref.getInterligne() - 1.0) * 10 + "px;");
        }
        
        ttsService.setSpeed(pref.getVitesseAudio());
    }

    private void renderDyslexiaText() {
        if (currentChapter == null || userPref == null) return;
        List<Text> nodes = dyslexiaService.processText(currentChapter.getContenu(), userPref);
        
        // Ajouter les écouteurs pour le dictionnaire
        for (Text node : nodes) {
            if (node.getUserData() instanceof String) {
                // Effet de survol
                node.setOnMouseEntered(e -> node.setUnderline(true));
                node.setOnMouseExited(e -> node.setUnderline(false));
                
                // Simple clic pour définir
                node.setOnMouseClicked(event -> {
                    String word = (String) node.getUserData();
                    showWordDefinition(word);
                });
            }
        }
        
        dyslexiaTextFlow.getChildren().setAll(nodes);
    }

    private void showWordDefinition(String word) {
        // Afficher un état de chargement si nécessaire
        dictionaryService.getDefinition(word).thenAccept(def -> {
            javafx.application.Platform.runLater(() -> {
                if (def != null) {
                    defEmptyState.setVisible(false);
                    defEmptyState.setManaged(false);
                    defFoundState.setVisible(true);
                    defFoundState.setManaged(true);

                    defWordTitle.setText(def.getWord().toUpperCase());
                    defPhonetic.setText(def.getPhonetic() != null ? "[" + def.getPhonetic() + "]" : "");
                    
                    definitionsContainer.getChildren().clear();
                    for (int i = 0; i < Math.min(5, def.getDefinitions().size()); i++) {
                        Label l = new Label("• " + def.getDefinitions().get(i));
                        l.setWrapText(true);
                        l.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
                        definitionsContainer.getChildren().add(l);
                    }
                    
                    synonymsContainer.getChildren().clear();
                    if (def.getSynonyms().isEmpty()) {
                        synonymsLabel.setVisible(false);
                        synonymsLabel.setManaged(false);
                    } else {
                        synonymsLabel.setVisible(true);
                        synonymsLabel.setManaged(true);
                        for (int i = 0; i < Math.min(10, def.getSynonyms().size()); i++) {
                            Label s = new Label(def.getSynonyms().get(i));
                            s.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5; -fx-text-fill: #6366f1; -fx-font-size: 11px;");
                            synonymsContainer.getChildren().add(s);
                        }
                    }
                } else {
                    // Optionnel: feedback si non trouvé
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    void onSwitchToFr(ActionEvent event) {
        dictionaryService.setLanguage("fr");
        btnLangFr.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnLangEn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8;");
    }

    @FXML
    void onSwitchToEn(ActionEvent event) {
        dictionaryService.setLanguage("en");
        btnLangEn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnLangFr.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8;");
    }

    @FXML
    void onManualSearch(ActionEvent event) {
        String word = dictSearchField.getText();
        if (word != null && !word.trim().isEmpty()) {
            showWordDefinition(word.trim());
        }
    }

    @FXML
    void onClearDictionary(ActionEvent event) {
        defFoundState.setVisible(false);
        defFoundState.setManaged(false);
        defEmptyState.setVisible(true);
        defEmptyState.setManaged(true);
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

        if (ch.getImageUrl() != null && !ch.getImageUrl().isEmpty()) {
            chapterBannerImage.setImage(new javafx.scene.image.Image(ch.getImageUrl(), true));
            chapterBannerImage.setVisible(true);
            chapterBannerImage.setManaged(true);
        } else {
            chapterBannerImage.setVisible(false);
            chapterBannerImage.setManaged(false);
        }

        
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
        
        renderDyslexiaText();
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
        if (contextChapters != null && currentChapter != null) {
            int index = contextChapters.indexOf(currentChapter);
            if (index > 0) setChapter(contextChapters.get(index - 1), contextChapters);
        }
    }

    @FXML
    void onNext(ActionEvent event) {
        if (contextChapters != null && currentChapter != null) {
            int index = contextChapters.indexOf(currentChapter);
            if (index < contextChapters.size() - 1) setChapter(contextChapters.get(index + 1), contextChapters);
        }
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
    void onPlayAudio(ActionEvent event) {
        if (currentChapter != null) {
            String text = currentChapter.getContenu();
            ttsService.speak(text, "fr-fr");
        }
    }

    @FXML
    void onToggleDyslexiaPanel(ActionEvent event) {
        boolean isVisible = dyslexiaPanelContainer.isVisible();
        dyslexiaPanelContainer.setVisible(!isVisible);
        if (!isVisible) {
            // Apply initial settings
            applyDyslexiaSettings(dyslexiaService.getPreferences(1));
        }
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
