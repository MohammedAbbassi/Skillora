package com.example.controllers;

import com.example.entities.Chapitre;
import com.example.entities.Cours;
import com.example.services.ChapitreService;
import com.example.services.CoursService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue Cours (cours.fxml).
 * Gère l'affichage de la liste des cours et les actions CRUD.
 */
public class CoursController implements Initializable {

    /* ── Injection FXML ─────────────────────────────────── */

    @FXML private Label         coursTitle;
    @FXML private Label         coursDescription;
    @FXML private ProgressBar   overallProgress;
    @FXML private Label         progressPercentLabel;
    @FXML private VBox          chapterListContainer;
    @FXML private ScrollPane    rootScrollPane;
    @FXML private VBox          mainContainer;

    // Barre de recherche / filtres (si présents dans le FXML)
    @FXML private TextField     searchField;
    @FXML private ComboBox<String> niveauFilter;

    /* ── Services ───────────────────────────────────────── */

    private final CoursService     coursService     = CoursService.getInstance();
    private final ChapitreService  chapitreService  = ChapitreService.getInstance();

    /* ── État ───────────────────────────────────────────── */

    private Cours coursActif;

    /* ── Initialisation ─────────────────────────────────── */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerFiltres();
        chargerPremierCours();
    }

    /** Remplit la ComboBox des niveaux. */
    private void configurerFiltres() {
        if (niveauFilter != null) {
            ObservableList<String> niveaux = FXCollections.observableArrayList(
                    "Tous", "Débutant", "Intermédiaire", "Avancé");
            niveauFilter.setItems(niveaux);
            niveauFilter.setValue("Tous");
            niveauFilter.setOnAction(e -> filtrerParNiveau());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> rechercherCours(val));
        }
    }

    /** Charge et affiche le premier cours disponible. */
    private void chargerPremierCours() {
        List<Cours> tous = coursService.trouverTous();
        if (!tous.isEmpty()) {
            afficherCours(tous.get(0));
        }
    }

    /* ── Affichage ──────────────────────────────────────── */

    /**
     * Remplit la vue avec les données du cours donné.
     */
    public void afficherCours(Cours cours) {
        this.coursActif = cours;

        if (coursTitle != null)       coursTitle.setText(cours.getTitre());
        if (coursDescription != null) coursDescription.setText(cours.getDescription());

        double prog = cours.getProgression();
        if (overallProgress != null)      overallProgress.setProgress(prog);
        if (progressPercentLabel != null) progressPercentLabel.setText(
                (int)(prog * 100) + "%");

        afficherChapitres(cours.getId());
    }

    /**
     * Charge et affiche les chapitres du cours dans {@code chapterListContainer}.
     * Délègue à {@link ChapitreController} le rendu de chaque carte.
     */
    private void afficherChapitres(int coursId) {
        if (chapterListContainer == null) return;
        chapterListContainer.getChildren().clear();

        List<Chapitre> chapitres = chapitreService.trouverParCours(coursId);
        for (Chapitre ch : chapitres) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/ressources/chapitre.fxml"));
                Parent carteNode = loader.load();

                ChapitreController ctrl = loader.getController();
                ctrl.initialiserCarte(ch, this);

                chapterListContainer.getChildren().add(carteNode);
            } catch (IOException e) {
                // Fallback : carte simple si le FXML échoue
                chapterListContainer.getChildren().add(creerCarteSimple(ch));
            }
        }
    }

    /** Carte de secours en cas d'erreur de chargement FXML. */
    private HBox creerCarteSimple(Chapitre ch) {
        HBox carte = new HBox(16);
        carte.getStyleClass().add("chapter-card");
        Label num   = new Label(String.format("%02d", ch.getNumero()));
        Label titre = new Label(ch.getTitre());
        carte.getChildren().addAll(num, titre);
        return carte;
    }

    /* ── Événements FXML ────────────────────────────────── */

    /**
     * Appelé par les boutons "Start" / "Continue" des cartes chapitre.
     * userData du bouton = numéro de chapitre.
     */
    @FXML
    public void onChapterAction(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String userData = (String) btn.getUserData();
        if (userData == null) return;

        int chapitreId = Integer.parseInt(userData);
        Chapitre ch = chapitreService.trouverParId(chapitreId);
        if (ch == null) return;

        ouvrirDetailChapitre(ch);
    }

    /** Ouvre la vue détail d'un chapitre dans une nouvelle fenêtre modale. */
    public void ouvrirDetailChapitre(Chapitre chapitre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/ressources/chapitre.fxml"));
            Parent root = loader.load();

            ChapitreController ctrl = loader.getController();
            ctrl.initialiserDetail(chapitre, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chapitre " + chapitre.getNumero() + " — " + chapitre.getTitre());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir après fermeture
            if (coursActif != null) afficherCours(coursActif);

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Impossible d'ouvrir le chapitre.", e.getMessage());
        }
    }

    /* ── Recherche & Filtres ────────────────────────────── */

    private void rechercherCours(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            chargerPremierCours();
            return;
        }
        List<Cours> resultats = coursService.rechercher(motCle);
        if (!resultats.isEmpty()) afficherCours(resultats.get(0));
    }

    private void filtrerParNiveau() {
        if (niveauFilter == null) return;
        String niveau = niveauFilter.getValue();
        List<Cours> liste = "Tous".equals(niveau)
                ? coursService.trouverTous()
                : coursService.trouverParNiveau(niveau);
        if (!liste.isEmpty()) afficherCours(liste.get(0));
    }

    /* ── Utilitaires ────────────────────────────────────── */

    /** Rafraîchit la vue du cours actif (après modification d'un chapitre). */
    public void rafraichir() {
        if (coursActif != null) {
            coursActif = coursService.trouverParId(coursActif.getId());
            if (coursActif != null) afficherCours(coursActif);
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ── Getter ─────────────────────────────────────────── */

    public Cours getCoursActif() { return coursActif; }
}
