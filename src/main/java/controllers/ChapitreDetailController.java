package controllers;

import entities.Chapitre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.util.List;

public class ChapitreDetailController {

    @FXML private Label chapterTitleLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressTextLabel;
    @FXML private WebView webView;
    @FXML private Label niveauLabel;
    @FXML private Label dureeLabel;
    @FXML private Label contenuLabel;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private Chapitre currentChapter;
    private List<Chapitre> contextChapters;

    public void setChapter(Chapitre ch, List<Chapitre> allChapters) {
        this.currentChapter = ch;
        this.contextChapters = allChapters;
        
        chapterTitleLabel.setText(ch.getTitre());
        niveauLabel.setText(ch.getNiveau() != null ? ch.getNiveau() : "MOYEN");
        niveauLabel.getStyleClass().removeAll("badge-facile", "badge-moyen", "badge-difficile");
        niveauLabel.getStyleClass().add("badge-" + niveauLabel.getText().toLowerCase());
        
        dureeLabel.setText(ch.getDuree() + " min");
        contenuLabel.setText(ch.getContenu());
        
        // Setup Video
        if (ch.getVideoUrl() != null && !ch.getVideoUrl().isEmpty()) {
            String embedUrl = ch.getVideoUrl();
            if (embedUrl.contains("youtube.com/watch?v=")) {
                embedUrl = embedUrl.replace("watch?v=", "embed/");
            }
            webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36");
            webView.getEngine().load(embedUrl);
        } else {
            webView.getEngine().loadContent("<div style='background: #1e293b; color: white; height: 100%; display: flex; align-items: center; justify-content: center; font-family: sans-serif;'>Vidéo non disponible</div>");
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

    @FXML void onQuiz(ActionEvent event) { /* Show Quiz */ }
    @FXML void onReadPdf(ActionEvent event) { /* Open PDF */ }
    @FXML void onGenerateSummary(ActionEvent event) { /* AI Summary mock */ }
}
