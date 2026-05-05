package com.example.controllers;

import com.example.entities.Chapitre;
import com.example.entities.Chapitre.Statut;
import com.example.services.ChapitreService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue Chapitre (chapitre.fxml).
 *
 * Ce contrôleur sert à deux usages :
 * <ul>
 *   <li><b>Mode carte</b> — rendu d'une carte dans la liste ({@link #initialiserCarte})</li>
 *   <li><b>Mode détail</b> — fenêtre modale de détail ({@link #initialiserDetail})</li>
 * </ul>
 */
public class ChapitreController implements Initializable {

    /* ── Injection FXML ─────────────────────────────────── */

    // -- Carte (liste)
    @FXML private HBox      chapterCardRoot;
    @FXML private StackPane chapitreNumCircle;
    @FXML private Label     chapitreNumLabel;
    @FXML private Circle    statusDot;
    @FXML private Label     chapitreTitre;
    @FXML private Label     statusTag;
    @FXML private Label     chapitreDuree;
    @FXML private Label     chapitreLecons;
    @FXML private Label     chapitreQuizTag;
    @FXML private ProgressBar chapitreProgressBar;
    @FXML private Button    actionButton;

    // -- Détail (modale)
    @FXML private Label     detailTitre;
    @FXML private Label     detailDescription;
    @FXML private Label     detailDuree;
    @FXML private Label     detailLecons;
    @FXML private Label     detailStatut;
    @FXML private Button    btnTerminer;
    @FXML private Button    btnFermer;

    /* ── Services ───────────────────────────────────────── */

    private final ChapitreService chapitreService = ChapitreService.getInstance();

    /* ── État ───────────────────────────────────────────── */

    private Chapitre     chapitre;
    private CoursController coursController;

    /* ── Initialisation Initializable ───────────────────── */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // L'initialisation réelle se fait via initialiserCarte() ou initialiserDetail()
    }

    /* ── API publique ───────────────────────────────────── */

    /**
     * Configure la carte chapitre pour la liste de cours.
     *
     * @param ch              Le chapitre à afficher
     * @param parentController Le contrôleur parent (CoursController)
     */
    public void initialiserCarte(Chapitre ch, CoursController parentController) {
        this.chapitre         = ch;
        this.coursController  = parentController;
        remplirCarte();
    }

    /**
     * Configure la vue détail (fenêtre modale).
     *
     * @param ch              Le chapitre à afficher
     * @param parentController Le contrôleur parent (CoursController)
     */
    public void initialiserDetail(Chapitre ch, CoursController parentController) {
        this.chapitre         = ch;
        this.coursController  = parentController;
        remplirDetail();
    }

    /* ── Remplissage Carte ──────────────────────────────── */

    private void remplirCarte() {
        if (chapitre == null) return;

        // Numéro
        if (chapitreNumLabel != null) {
            chapitreNumLabel.setText(chapitre.getStatut() == Statut.TERMINE
                    ? "✓" : String.format("%02d", chapitre.getNumero()));
        }

        // Titre
        if (chapitreTitre != null) chapitreTitre.setText(chapitre.getTitre());

        // Méta
        if (chapitreDuree  != null) chapitreDuree.setText("⏱  " + chapitre.getDureeFormatee());
        if (chapitreLecons != null) chapitreLecons.setText(chapitre.getNombreLecons() + " leçons");

        if (chapitreQuizTag != null) {
            chapitreQuizTag.setVisible(chapitre.isAvecQuiz());
            chapitreQuizTag.setManaged(chapitre.isAvecQuiz());
        }

        // Progress bar (visible seulement si EN_COURS)
        if (chapitreProgressBar != null) {
            boolean enCours = chapitre.getStatut() == Statut.EN_COURS;
            chapitreProgressBar.setVisible(enCours);
            chapitreProgressBar.setManaged(enCours);
        }

        // Style selon statut
        appliquerStyleStatut();

        // Bouton
        configurerBoutonCarte();
    }

    private void appliquerStyleStatut() {
        if (chapterCardRoot == null) return;
        chapterCardRoot.getStyleClass().removeAll(
                "chapter-card-done", "chapter-card-active", "chapter-card-locked");

        // Status dot
        if (statusDot != null) {
            statusDot.getStyleClass().removeAll("status-done", "status-progress", "status-locked");
        }
        // Status tag
        if (statusTag != null) {
            statusTag.getStyleClass().removeAll("status-tag-done", "status-tag-progress");
        }
        // Cercle numéro
        if (chapitreNumCircle != null) {
            chapitreNumCircle.getStyleClass().removeAll(
                    "chapter-num-done", "chapter-num-active", "chapter-num-locked");
        }

        switch (chapitre.getStatut()) {
            case TERMINE -> {
                chapterCardRoot.getStyleClass().add("chapter-card-done");
                if (statusDot     != null) statusDot.getStyleClass().add("status-done");
                if (statusTag     != null) { statusTag.setText("TERMINÉ"); statusTag.getStyleClass().add("status-tag-done"); statusTag.setVisible(true); }
                if (chapitreNumCircle != null) chapitreNumCircle.getStyleClass().add("chapter-num-done");
            }
            case EN_COURS -> {
                chapterCardRoot.getStyleClass().add("chapter-card-active");
                if (statusDot     != null) statusDot.getStyleClass().add("status-progress");
                if (statusTag     != null) { statusTag.setText("EN COURS"); statusTag.getStyleClass().add("status-tag-progress"); statusTag.setVisible(true); }
                if (chapitreNumCircle != null) chapitreNumCircle.getStyleClass().add("chapter-num-active");
            }
            case VERROUILLE -> {
                chapterCardRoot.getStyleClass().add("chapter-card-locked");
                if (statusDot     != null) statusDot.getStyleClass().add("status-locked");
                if (statusTag     != null) statusTag.setVisible(false);
                if (chapitreNumCircle != null) chapitreNumCircle.getStyleClass().add("chapter-num-locked");
            }
        }
    }

    private void configurerBoutonCarte() {
        if (actionButton == null) return;
        switch (chapitre.getStatut()) {
            case TERMINE    -> { actionButton.setText("Revoir");      actionButton.getStyleClass().setAll("btn-secondary"); }
            case EN_COURS   -> { actionButton.setText("Continuer →"); actionButton.getStyleClass().setAll("btn-primary");   }
            case VERROUILLE -> { actionButton.setText("Démarrer");    actionButton.getStyleClass().setAll("btn-start");     }
        }
        actionButton.setOnAction(e -> onActionBouton());
    }

    /* ── Remplissage Détail ─────────────────────────────── */

    private void remplirDetail() {
        if (chapitre == null) return;
        if (detailTitre       != null) detailTitre.setText(chapitre.getTitre());
        if (detailDescription != null) detailDescription.setText(
                chapitre.getDescription().isEmpty()
                ? "Aucune description disponible pour ce chapitre."
                : chapitre.getDescription());
        if (detailDuree   != null) detailDuree.setText("Durée : " + chapitre.getDureeFormatee());
        if (detailLecons  != null) detailLecons.setText("Leçons : " + chapitre.getNombreLecons());
        if (detailStatut  != null) detailStatut.setText("Statut : " + chapitre.getStatut().name());

        if (btnTerminer != null) {
            btnTerminer.setDisable(chapitre.getStatut() == Statut.TERMINE);
        }
    }

    /* ── Événements ─────────────────────────────────────── */

    /** Déclenchement depuis la carte (bouton Démarrer / Continuer / Revoir). */
    private void onActionBouton() {
        if (chapitre == null) return;
        switch (chapitre.getStatut()) {
            case VERROUILLE -> chapitreService.demarrerChapitre(chapitre.getId());
            case EN_COURS, TERMINE -> {
                if (coursController != null) coursController.ouvrirDetailChapitre(chapitre);
                return;
            }
        }
        remplirCarte();
        if (coursController != null) coursController.rafraichir();
    }

    /** Bouton "Marquer comme terminé" dans la vue détail. */
    @FXML
    private void onTerminer() {
        if (chapitre == null) return;
        chapitreService.terminerChapitre(chapitre.getId());

        // Recharger l'entité mise à jour
        chapitre = chapitreService.trouverParId(chapitre.getId());
        remplirDetail();

        if (coursController != null) coursController.rafraichir();

        afficherInfo("Chapitre terminé !",
                "Bravo ! Le chapitre suivant est maintenant déverrouillé.");
    }

    /** Bouton "Fermer" dans la vue détail. */
    @FXML
    private void onFermer() {
        if (btnFermer != null) {
            Stage stage = (Stage) btnFermer.getScene().getWindow();
            stage.close();
        }
    }

    /* ── Utilitaires ────────────────────────────────────── */

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
