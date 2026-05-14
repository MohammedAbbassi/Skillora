package com.skillora.controllers;

import entities.Evenement;
import entities.Role;
import services.LocationService;
import services.EvenementCRUD;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EvenementController implements Initializable {

    @FXML private TextField txtNom;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtLieu;
    @FXML private TextField txtDuree;
    @FXML private TextField txtImage;
    @FXML private TextField txtRechercheEvenement;
    @FXML private ComboBox<String> comboFiltreDate;
    @FXML private Label lblCurrentUser;
    @FXML private ImageView imageView;
    @FXML private Label lblImageStatus;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnulerModification;
    @FXML private Button btnChoisirImage;
    @FXML private Button btnVerifierLocalisation;
    @FXML private Button btnOpenEventForm;
    @FXML private Button btnOpenSelectedEventForm;
    @FXML private Button btnListSupprimer;
    @FXML private VBox eventListPage;
    @FXML private VBox eventFormPage;
    @FXML private VBox actionBox;
    @FXML private HBox rowEditActions;
    
    @FXML private ListView<Evenement> listEvenements;

    private final EvenementCRUD evenementCRUD = new EvenementCRUD();
    private final LocationService locationService = new LocationService();
    private ObservableList<Evenement> allEventList = FXCollections.observableArrayList();
    private ObservableList<Evenement> eventList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preparerListeEvenements();
        preparerFiltresEvenements();
        preparerApercuImage();
        preparerCalendrier();
        afficherUtilisateurConnecte();
        chargerEvenementsDepuisBase();
        afficherListeEvenements();
    }

    private void preparerCalendrier() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void afficherUtilisateurConnecte() {
        lblCurrentUser.setText(SessionManager.getCurrentUserName()
                + " | Role: " + SessionManager.getCurrentUserRoleLabel());
        appliquerPermissionsSelonRole(null);
    }

    private void preparerListeEvenements() {
        listEvenements.setCellFactory(list -> creerLigneListeEvenement());
        listEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
            appliquerPermissionsSelonRole(newSelection);
        });
    }

    private void preparerFiltresEvenements() {
        comboFiltreDate.getItems().setAll("Tous", "A venir", "Passes");
        comboFiltreDate.setValue("Tous");
        txtRechercheEvenement.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltresEvenements());
        comboFiltreDate.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltresEvenements());
    }

    private void appliquerPermissionsSelonRole(Evenement selectedEvent) {
        boolean admin = SessionManager.getCurrentUserRole() == Role.ADMIN;
        boolean canCreate = peutCreerEvenement();
        boolean canManageSelected = peutGererEvenement(selectedEvent);
        boolean hasSelection = selectedEvent != null;

        txtNom.setDisable(!admin);
        datePicker.setDisable(!admin);
        txtLieu.setDisable(!admin);
        txtDuree.setDisable(!admin);
        txtImage.setDisable(!admin);
        btnChoisirImage.setDisable(!admin);
        btnVerifierLocalisation.setDisable(!admin);

        rendreElementVisible(btnOpenEventForm, admin);
        rendreElementVisible(btnOpenSelectedEventForm, admin);
        rendreElementVisible(btnListSupprimer, admin);
        btnOpenEventForm.setDisable(!canCreate);
        btnOpenSelectedEventForm.setDisable(!canManageSelected);
        btnListSupprimer.setDisable(!canManageSelected);

        rendreElementVisible(actionBox, admin);
        rendreElementVisible(btnAjouter, admin && !hasSelection);
        rendreElementVisible(rowEditActions, admin && hasSelection);
        btnAjouter.setDisable(!canCreate);
        btnModifier.setDisable(!canManageSelected);
        btnAnnulerModification.setDisable(!hasSelection);
    }

    private void rendreElementVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private boolean peutCreerEvenement() {
        Role role = SessionManager.getCurrentUserRole();
        return role == Role.ADMIN;
    }

    private boolean peutGererEvenement(Evenement evenement) {
        if (evenement == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        return role == Role.ADMIN;
    }

    private ListCell<Evenement> creerLigneListeEvenement() {
        return new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(12);
                row.getStyleClass().add("event-list-row");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                ImageView eventImage = new ImageView();
                eventImage.setFitWidth(92);
                eventImage.setFitHeight(58);
                eventImage.setPreserveRatio(true);
                eventImage.getStyleClass().add("event-list-image");
                if (evenement.getImage() != null && !evenement.getImage().trim().isEmpty()) {
                    try {
                        Image image = new Image(resoudreSourceImage(evenement.getImage()), 92, 58, true, true, true);
                        eventImage.setImage(image.isError() ? null : image);
                    } catch (Exception e) {
                        eventImage.setImage(null);
                    }
                }

                VBox info = new VBox(5);
                info.setPrefWidth(520);

                Label title = new Label(evenement.getNom());
                title.setWrapText(true);
                title.getStyleClass().add("event-list-title");

                Label meta = new Label("Date: " + evenement.getDate_evenement()
                        + " | Lieu: " + evenement.getLieu()
                        + " | Duree: " + formaterDuree(evenement.getDuree_minutes()));
                meta.setWrapText(true);
                meta.getStyleClass().add("event-list-meta");

                info.getChildren().addAll(title, meta);
                row.getChildren().addAll(eventImage, info);

                setText(null);
                setGraphic(row);
            }
        };
    }

    private String formaterDuree(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + " h";
        }
        return hours + " h " + remainingMinutes + " min";
    }

    private void remplirFormulaire(Evenement e) {
        txtNom.setText(e.getNom());
        datePicker.setValue(e.getDate_evenement().toLocalDate());
        txtLieu.setText(e.getLieu());
        txtDuree.setText(String.valueOf(e.getDuree_minutes()));
        txtImage.setText(e.getImage());
        mettreAJourImage(e.getImage());
        retirerErreursVisuelles();
    }

    private Evenement recupererEvenementSelectionne() {
        return listEvenements.getSelectionModel().getSelectedItem();
    }

    private void preparerApercuImage() {
        imageView.setPreserveRatio(true);
        afficherApercuImageVide();
        txtImage.setOnAction(event -> mettreAJourImage(txtImage.getText()));
        txtImage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                mettreAJourImage(txtImage.getText());
            }
        });
    }

    @FXML
    private void choisirImageEvenement(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(txtImage.getScene().getWindow());
        if (selectedFile != null) {
            txtImage.setText(selectedFile.getAbsolutePath());
            mettreAJourImage(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void verifierLocalisationEvenement(ActionEvent event) {
        choisirLocalisationEvenement(event);
    }

    @FXML
    private void choisirLocalisationEvenement(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(txtLieu.getText());
        dialog.setTitle("Choisir une localisation");
        dialog.setHeaderText(null);
        dialog.setContentText("Ville ou adresse :");
        appliquerStyleAlerte(dialog, "alert-confirmation");

        dialog.showAndWait().ifPresent(query -> {
            String search = query == null ? "" : query.trim();
            if (search.isEmpty()) {
                afficherAlerte("Localisation requise", "Veuillez saisir une ville ou une adresse.", Alert.AlertType.WARNING);
                return;
            }

            try {
                LocationService.LocationResult result = locationService.rechercher(search);
                txtLieu.setText(result.getLabel());

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Localisation trouvee");
                confirm.setHeaderText(null);
                confirm.setContentText("Localisation selectionnee :\n"
                        + result.getLabel()
                        + "\n\nVoulez-vous l'ouvrir dans Google Maps ?");
                appliquerStyleAlerte(confirm, "alert-confirmation");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    ouvrirLienExterne("https://www.google.com/maps/search/?api=1&query="
                            + URLEncoder.encode(result.getLabel(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                afficherAlerte("Localisation introuvable",
                        "Impossible de trouver cette localisation. Essayez une ville ou une adresse plus precise.",
                        Alert.AlertType.WARNING);
            }
        });
    }

    @FXML
    private void ouvrirLocalisationEvenement(ActionEvent event) {
        String lieu = txtLieu.getText() == null ? "" : txtLieu.getText().trim();
        String nom = txtNom.getText() == null ? "" : txtNom.getText().trim();

        if (lieu.isEmpty()) {
            afficherAlerte("Lieu requis", "Veuillez saisir le lieu de l'evenement avant de verifier la localisation.", Alert.AlertType.WARNING);
            return;
        }

        String query = nom.isEmpty() ? lieu : lieu + " " + nom;
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        ouvrirLienExterne("https://www.google.com/maps/search/?api=1&query=" + encodedQuery);
    }

    private void ouvrirLienExterne(String url) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                afficherAlerte("Navigation indisponible", "Votre systeme ne permet pas d'ouvrir le navigateur automatiquement.", Alert.AlertType.WARNING);
                return;
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la localisation de cet evenement.", Alert.AlertType.ERROR);
        }
    }

    private void mettreAJourImage(String source) {
        if (source == null || source.trim().isEmpty()) {
            afficherApercuImageVide();
            return;
        }

        try {
            Image img = new Image(resoudreSourceImage(source), 260, 125, true, true, false);
            if (img.isError()) {
                afficherApercuImageInvalide();
                return;
            }

            imageView.setImage(img);
            lblImageStatus.setText("Image selectionnee");
        } catch (Exception e) {
            afficherApercuImageInvalide();
        }
    }

    private String resoudreSourceImage(String source) {
        String trimmedSource = source.trim();
        if (trimmedSource.startsWith("http://")
                || trimmedSource.startsWith("https://")
                || trimmedSource.startsWith("file:")) {
            return trimmedSource;
        }
        if (trimmedSource.startsWith("classpath:")) {
            String resourcePath = trimmedSource.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
            if (resource != null) {
                return resource.toExternalForm();
            }
        }
        return new File(trimmedSource).toURI().toString();
    }

    private void afficherApercuImageVide() {
        imageView.setImage(null);
        lblImageStatus.setText("Aucune image choisie");
    }

    private void afficherApercuImageInvalide() {
        imageView.setImage(null);
        lblImageStatus.setText("Image introuvable ou format invalide");
    }

    private void chargerEvenementsDepuisBase() {
        try {
            java.util.List<Evenement> data = evenementCRUD.afficher();
            allEventList.setAll(data);
            appliquerFiltresEvenements();
        } catch (Exception e) {
            afficherAlerte("Erreur de connexion", "Impossible de contacter la base de donnees. Assurez-vous que MySQL est lance sur le port 3306.", Alert.AlertType.ERROR);
        }
    }

    private void appliquerFiltresEvenements() {
        String search = txtRechercheEvenement.getText() != null
                ? txtRechercheEvenement.getText().trim().toLowerCase()
                : "";
        String dateFilter = comboFiltreDate.getValue() != null ? comboFiltreDate.getValue() : "Tous";

        List<Evenement> filteredEvents = new ArrayList<>();
        for (Evenement evenement : allEventList) {
            if (!correspondAuFiltreDate(evenement, dateFilter)) {
                continue;
            }
            if (!correspondARechercheEvenement(evenement, search)) {
                continue;
            }
            filteredEvents.add(evenement);
        }

        eventList.setAll(filteredEvents);
        listEvenements.setItems(eventList);
        appliquerPermissionsSelonRole(recupererEvenementSelectionne());
    }

    private boolean correspondAuFiltreDate(Evenement evenement, String dateFilter) {
        if ("Tous".equals(dateFilter)) {
            return true;
        }

        LocalDate eventDate = evenement.getDate_evenement().toLocalDate();
        if ("A venir".equals(dateFilter)) {
            return !eventDate.isBefore(LocalDate.now());
        }
        return eventDate.isBefore(LocalDate.now());
    }

    private boolean correspondARechercheEvenement(Evenement evenement, String search) {
        if (search.isEmpty()) {
            return true;
        }

        String content = (evenement.getNom() + " "
                + evenement.getLieu() + " "
                + evenement.getDate_evenement() + " "
                + formaterDuree(evenement.getDuree_minutes())).toLowerCase();
        return content.contains(search);
    }

    @FXML
    private void ouvrirFormulaireAjout(ActionEvent event) {
        listEvenements.getSelectionModel().clearSelection();
        viderChampsFormulaire();
        appliquerPermissionsSelonRole(null);
        afficherFormulaireEvenement();
    }

    @FXML
    private void ouvrirFormulaireModification(ActionEvent event) {
        Evenement selected = recupererEvenementSelectionne();
        if (selected == null) {
            afficherAlerte("Selection requise", "Veuillez selectionner un evenement dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        remplirFormulaire(selected);
        appliquerPermissionsSelonRole(selected);
        afficherFormulaireEvenement();
    }

    @FXML
    private void retournerALaListeEvenements(ActionEvent event) {
        afficherListeEvenements();
    }

    private void afficherListeEvenements() {
        rendreElementVisible(eventListPage, true);
        rendreElementVisible(eventFormPage, false);
    }

    private void afficherFormulaireEvenement() {
        rendreElementVisible(eventListPage, false);
        rendreElementVisible(eventFormPage, true);
    }

    @FXML
    private void ajouterDepuisFormulaire(ActionEvent event) {
        if (!peutCreerEvenement()) {
            afficherAlerte("Acces refuse", "Seul l'admin peut ajouter un evenement.", Alert.AlertType.WARNING);
            return;
        }
        Evenement selected = recupererEvenementSelectionne();
        if (selected != null && evenementSelectionneSansChangement(selected)) {
            afficherAlerte("Mode modification", "Cet evenement est deja selectionne. Utilisez Modifier pour le changer.", Alert.AlertType.WARNING);
            return;
        }

        if (validerSaisieFormulaire()) {
            Evenement e = creerEvenementDepuisFormulaire();
            try {
                evenementCRUD.ajouter(e);
                chargerEvenementsDepuisBase();
                viderFormulaire();
                afficherAlerte("Ajout reussi", "L'evenement a ete ajoute avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean evenementSelectionneSansChangement(Evenement selected) {
        if (datePicker.getValue() == null) {
            return false;
        }

        String selectedImage = selected.getImage() != null ? selected.getImage().trim() : "";
        String formImage = txtImage.getText() != null ? txtImage.getText().trim() : "";

        return selected.getNom().trim().equalsIgnoreCase(txtNom.getText().trim())
                && selected.getDate_evenement().toLocalDate().equals(datePicker.getValue())
                && selected.getLieu().trim().equalsIgnoreCase(txtLieu.getText().trim())
                && String.valueOf(selected.getDuree_minutes()).equals(txtDuree.getText().trim())
                && selectedImage.equals(formImage);
    }

    @FXML
    private void modifierDepuisFormulaire(ActionEvent event) {
        Evenement selected = recupererEvenementSelectionne();
        if (!peutGererEvenement(selected)) {
            afficherAlerte("Acces refuse", "Seul l'admin peut modifier un evenement.", Alert.AlertType.WARNING);
            return;
        }

        if (validerSaisieFormulaire()) {
            if (evenementSelectionneSansChangement(selected)) {
                afficherAlerte("Aucune modification", "Aucune modification detectee.", Alert.AlertType.INFORMATION);
                return;
            }

            Evenement e = creerEvenementDepuisFormulaire(selected.getId_utilisateur());
            e.setId_evenement(selected.getId_evenement());
            try {
                evenementCRUD.modifier(e);
                chargerEvenementsDepuisBase();
                viderFormulaire();
                afficherAlerte("Modification reussie", "L'evenement a ete modifie avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void annulerModificationFormulaire(ActionEvent event) {
        viderFormulaire();
    }

    @FXML
    private void supprimerElementSelectionne(ActionEvent event) {
        Evenement selected = recupererEvenementSelectionne();
        if (selected == null) {
            afficherAlerte("Selection requise", "Veuillez selectionner un evenement dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        if (!peutGererEvenement(selected)) {
            afficherAlerte("Acces refuse", "Seul l'admin peut supprimer un evenement.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l'evenement");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cet evenement ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            evenementCRUD.supprimer(selected.getId_evenement());
            chargerEvenementsDepuisBase();
            viderFormulaire();
            afficherAlerte("Suppression reussie", "L'evenement a ete supprime.", Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Evenement creerEvenementDepuisFormulaire() {
        return creerEvenementDepuisFormulaire(SessionManager.getCurrentUserId());
    }

    private Evenement creerEvenementDepuisFormulaire(long ownerId) {
        Evenement e = new Evenement();
        e.setNom(txtNom.getText().trim());
        e.setDate_evenement(Date.valueOf(datePicker.getValue()));
        e.setLieu(txtLieu.getText().trim());
        e.setDuree_minutes(Integer.parseInt(txtDuree.getText().trim()));
        e.setImage(txtImage.getText().trim());
        e.setId_utilisateur(ownerId);
        return e;
    }

    private void viderFormulaire() {
        viderChampsFormulaire();
        listEvenements.getSelectionModel().clearSelection();
        appliquerPermissionsSelonRole(null);
        afficherListeEvenements();
    }

    private void viderChampsFormulaire() {
        txtNom.clear();
        datePicker.setValue(null);
        txtLieu.clear();
        txtDuree.clear();
        txtImage.clear();
        afficherApercuImageVide();
        retirerErreursVisuelles();
    }

    private boolean validerSaisieFormulaire() {
        retirerErreursVisuelles();
        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        String nom = txtNom.getText() == null ? "" : txtNom.getText().trim();
        String lieu = txtLieu.getText() == null ? "" : txtLieu.getText().trim();
        String dureeText = txtDuree.getText() == null ? "" : txtDuree.getText().trim();
        String image = txtImage.getText() == null ? "" : txtImage.getText().trim();

        if (nom.isEmpty()) {
            marquerChampInvalide(txtNom);
            errors.append("- Le nom de l'evenement est obligatoire.\n");
            valid = false;
        } else if (nom.length() < 3 || nom.length() > 50) {
            marquerChampInvalide(txtNom);
            errors.append("- Le nom doit contenir entre 3 et 50 caracteres.\n");
            valid = false;
        }

        if (datePicker.getValue() == null) {
            marquerChampInvalide(datePicker);
            errors.append("- La date de l'evenement est obligatoire.\n");
            valid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            marquerChampInvalide(datePicker);
            errors.append("- La date de l'evenement ne doit pas etre avant aujourd'hui.\n");
            valid = false;
        }

        if (lieu.isEmpty()) {
            marquerChampInvalide(txtLieu);
            errors.append("- Le lieu est obligatoire.\n");
            valid = false;
        } else if (lieu.length() < 3 || lieu.length() > 50) {
            marquerChampInvalide(txtLieu);
            errors.append("- Le lieu doit contenir entre 3 et 50 caracteres.\n");
            valid = false;
        }

        if (dureeText.isEmpty()) {
            marquerChampInvalide(txtDuree);
            errors.append("- La duree est obligatoire.\n");
            valid = false;
        } else {
            try {
                int duree = Integer.parseInt(dureeText);
                if (duree < 15 || duree > 480) {
                    marquerChampInvalide(txtDuree);
                    errors.append("- La duree doit etre entre 15 et 480 minutes.\n");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                marquerChampInvalide(txtDuree);
                errors.append("- La duree doit etre un nombre entier en minutes.\n");
                valid = false;
            }
        }

        if (!image.isEmpty() && !sourceImageEstValide(image)) {
            marquerChampInvalide(txtImage);
            errors.append("- L'image doit etre une URL valide, un fichier existant ou une ressource classpath.\n");
            valid = false;
        }

        if (SessionManager.getCurrentUserId() <= 0) {
            errors.append("- Aucun utilisateur connecte.\n");
            valid = false;
        }

        if (!valid) {
            afficherAlerte("Controle de saisie", errors.toString(), Alert.AlertType.WARNING);
        }

        return valid;
    }

    private boolean sourceImageEstValide(String source) {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            try {
                new URL(source).toURI();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        if (source.startsWith("classpath:")) {
            String resourcePath = source.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            return Thread.currentThread().getContextClassLoader().getResource(resourcePath) != null;
        }

        if (source.startsWith("file:")) {
            try {
                return new File(new URL(source).toURI()).exists();
            } catch (Exception e) {
                return false;
            }
        }

        return new File(source).exists();
    }

    private void marquerChampInvalide(Control c) {
        c.getStyleClass().add("field-error");
    }

    private void retirerErreursVisuelles() {
        txtNom.getStyleClass().remove("field-error");
        datePicker.getStyleClass().remove("field-error");
        txtLieu.getStyleClass().remove("field-error");
        txtDuree.getStyleClass().remove("field-error");
        txtImage.getStyleClass().remove("field-error");
    }

    private void afficherAlerte(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        appliquerStyleAlerte(alert, classeStyleSelonTypeAlerte(type));
        alert.showAndWait();
    }

    private void appliquerStyleAlerte(Dialog<?> dialog, String styleClass) {
        String stylesheet = getClass().getResource("/com/skillora/style.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(stylesheet);
        dialog.getDialogPane().getStyleClass().add("alert-dialog");
        dialog.getDialogPane().getStyleClass().add(styleClass);
    }

    private String classeStyleSelonTypeAlerte(Alert.AlertType type) {
        if (type == Alert.AlertType.ERROR) {
            return "alert-error";
        }
        if (type == Alert.AlertType.WARNING) {
            return "alert-warning";
        }
        if (type == Alert.AlertType.INFORMATION) {
            return "alert-success";
        }
        return "alert-confirmation";
    }
}


