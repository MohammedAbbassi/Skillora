package com.skillora.controllers;

import entities.Evenement;
import entities.Role;
import services.EvenementCRUD;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
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
    @FXML private Button btnOpenEventForm;
    @FXML private Button btnOpenSelectedEventForm;
    @FXML private Button btnListSupprimer;
    @FXML private VBox eventListPage;
    @FXML private VBox eventFormPage;
    @FXML private VBox actionBox;
    @FXML private HBox rowEditActions;
    
    @FXML private ListView<Evenement> listEvenements;

    private final EvenementCRUD evenementCRUD = new EvenementCRUD();
    private ObservableList<Evenement> allEventList = FXCollections.observableArrayList();
    private ObservableList<Evenement> eventList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupEventList();
        setupEventFilters();
        setupImagePreview();
        setupDatePicker();
        setupCurrentUser();
        loadEvenements();
        showEventListPage();
    }

    private void setupDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void setupCurrentUser() {
        lblCurrentUser.setText(SessionManager.getCurrentUserName()
                + " | Role: " + SessionManager.getCurrentUserRoleLabel());
        applyRolePermissions(null);
    }

    private void setupEventList() {
        listEvenements.setCellFactory(list -> createEventListCell());
        listEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
            applyRolePermissions(newSelection);
        });
    }

    private void setupEventFilters() {
        comboFiltreDate.getItems().setAll("Tous", "A venir", "Passes");
        comboFiltreDate.setValue("Tous");
        txtRechercheEvenement.textProperty().addListener((observable, oldValue, newValue) -> applyEventFilters());
        comboFiltreDate.valueProperty().addListener((observable, oldValue, newValue) -> applyEventFilters());
    }

    private void applyRolePermissions(Evenement selectedEvent) {
        boolean admin = SessionManager.getCurrentUserRole() == Role.ADMIN;
        boolean canCreate = canCreateEvent();
        boolean canManageSelected = canManageEvent(selectedEvent);
        boolean hasSelection = selectedEvent != null;

        txtNom.setDisable(!admin);
        datePicker.setDisable(!admin);
        txtLieu.setDisable(!admin);
        txtDuree.setDisable(!admin);
        txtImage.setDisable(!admin);
        btnChoisirImage.setDisable(!admin);

        setNodeVisible(btnOpenEventForm, admin);
        setNodeVisible(btnOpenSelectedEventForm, admin);
        setNodeVisible(btnListSupprimer, admin);
        btnOpenEventForm.setDisable(!canCreate);
        btnOpenSelectedEventForm.setDisable(!canManageSelected);
        btnListSupprimer.setDisable(!canManageSelected);

        setNodeVisible(actionBox, admin);
        setNodeVisible(btnAjouter, admin && !hasSelection);
        setNodeVisible(rowEditActions, admin && hasSelection);
        btnAjouter.setDisable(!canCreate);
        btnModifier.setDisable(!canManageSelected);
        btnAnnulerModification.setDisable(!hasSelection);
    }

    private void setNodeVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private boolean canCreateEvent() {
        Role role = SessionManager.getCurrentUserRole();
        return role == Role.ADMIN;
    }

    private boolean canManageEvent(Evenement evenement) {
        if (evenement == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        return role == Role.ADMIN;
    }

    private ListCell<Evenement> createEventListCell() {
        return new ListCell<>() {
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
                        Image image = new Image(resolveImageSource(evenement.getImage()), 92, 58, true, true, true);
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
                        + " | Duree: " + formatDuration(evenement.getDuree_minutes()));
                meta.setWrapText(true);
                meta.getStyleClass().add("event-list-meta");

                info.getChildren().addAll(title, meta);
                row.getChildren().addAll(eventImage, info);

                setText(null);
                setGraphic(row);
            }
        };
    }

    private String formatDuration(int minutes) {
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

    private void populateFields(Evenement e) {
        txtNom.setText(e.getNom());
        datePicker.setValue(e.getDate_evenement().toLocalDate());
        txtLieu.setText(e.getLieu());
        txtDuree.setText(String.valueOf(e.getDuree_minutes()));
        txtImage.setText(e.getImage());
        updateImage(e.getImage());
        resetValidationStyles();
    }

    private Evenement getSelectedEvent() {
        return listEvenements.getSelectionModel().getSelectedItem();
    }

    private void setupImagePreview() {
        imageView.setPreserveRatio(true);
        showEmptyImagePreview();
        txtImage.setOnAction(event -> updateImage(txtImage.getText()));
        txtImage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                updateImage(txtImage.getText());
            }
        });
    }

    @FXML
    private void handleChoisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(txtImage.getScene().getWindow());
        if (selectedFile != null) {
            txtImage.setText(selectedFile.getAbsolutePath());
            updateImage(selectedFile.getAbsolutePath());
        }
    }

    private void updateImage(String source) {
        if (source == null || source.trim().isEmpty()) {
            showEmptyImagePreview();
            return;
        }

        try {
            Image img = new Image(resolveImageSource(source), 260, 125, true, true, false);
            if (img.isError()) {
                showInvalidImagePreview();
                return;
            }

            imageView.setImage(img);
            lblImageStatus.setText("Image selectionnee");
        } catch (Exception e) {
            showInvalidImagePreview();
        }
    }

    private String resolveImageSource(String source) {
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

    private void showEmptyImagePreview() {
        imageView.setImage(null);
        lblImageStatus.setText("Aucune image choisie");
    }

    private void showInvalidImagePreview() {
        imageView.setImage(null);
        lblImageStatus.setText("Image introuvable ou format invalide");
    }

    private void loadEvenements() {
        try {
            java.util.List<Evenement> data = evenementCRUD.afficher();
            allEventList.setAll(data);
            applyEventFilters();
        } catch (Exception e) {
            showAlert("Erreur de connexion", "Impossible de contacter la base de donnees. Assurez-vous que MySQL est lance sur le port 3306.", Alert.AlertType.ERROR);
        }
    }

    private void applyEventFilters() {
        String search = txtRechercheEvenement.getText() != null
                ? txtRechercheEvenement.getText().trim().toLowerCase()
                : "";
        String dateFilter = comboFiltreDate.getValue() != null ? comboFiltreDate.getValue() : "Tous";

        List<Evenement> filteredEvents = new ArrayList<>();
        for (Evenement evenement : allEventList) {
            if (!matchesDateFilter(evenement, dateFilter)) {
                continue;
            }
            if (!matchesEventSearch(evenement, search)) {
                continue;
            }
            filteredEvents.add(evenement);
        }

        eventList.setAll(filteredEvents);
        listEvenements.setItems(eventList);
        applyRolePermissions(getSelectedEvent());
    }

    private boolean matchesDateFilter(Evenement evenement, String dateFilter) {
        if ("Tous".equals(dateFilter)) {
            return true;
        }

        LocalDate eventDate = evenement.getDate_evenement().toLocalDate();
        if ("A venir".equals(dateFilter)) {
            return !eventDate.isBefore(LocalDate.now());
        }
        return eventDate.isBefore(LocalDate.now());
    }

    private boolean matchesEventSearch(Evenement evenement, String search) {
        if (search.isEmpty()) {
            return true;
        }

        String content = (evenement.getNom() + " "
                + evenement.getLieu() + " "
                + evenement.getDate_evenement() + " "
                + formatDuration(evenement.getDuree_minutes())).toLowerCase();
        return content.contains(search);
    }

    @FXML
    private void handleOpenAddForm() {
        listEvenements.getSelectionModel().clearSelection();
        clearFormFields();
        applyRolePermissions(null);
        showEventFormPage();
    }

    @FXML
    private void handleOpenSelectedForm() {
        Evenement selected = getSelectedEvent();
        if (selected == null) {
            showAlert("Selection requise", "Veuillez selectionner un evenement dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        populateFields(selected);
        applyRolePermissions(selected);
        showEventFormPage();
    }

    @FXML
    private void handleBackToEventList() {
        showEventListPage();
    }

    private void showEventListPage() {
        setNodeVisible(eventListPage, true);
        setNodeVisible(eventFormPage, false);
    }

    private void showEventFormPage() {
        setNodeVisible(eventListPage, false);
        setNodeVisible(eventFormPage, true);
    }

    @FXML
    private void handleAjouter() {
        if (!canCreateEvent()) {
            showAlert("Acces refuse", "Seul l'admin peut ajouter un evenement.", Alert.AlertType.WARNING);
            return;
        }
        Evenement selected = getSelectedEvent();
        if (selected != null && isSelectedEventUnchanged(selected)) {
            showAlert("Mode modification", "Cet evenement est deja selectionne. Utilisez Modifier pour le changer.", Alert.AlertType.WARNING);
            return;
        }

        if (validateInput()) {
            Evenement e = createEventFromFields();
            try {
                evenementCRUD.ajouter(e);
                loadEvenements();
                clearForm();
                showAlert("Ajout reussi", "L'evenement a ete ajoute avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean isSelectedEventUnchanged(Evenement selected) {
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
    private void handleModifier() {
        Evenement selected = getSelectedEvent();
        if (!canManageEvent(selected)) {
            showAlert("Acces refuse", "Seul l'admin peut modifier un evenement.", Alert.AlertType.WARNING);
            return;
        }

        if (validateInput()) {
            if (isSelectedEventUnchanged(selected)) {
                showAlert("Aucune modification", "Aucune modification detectee.", Alert.AlertType.INFORMATION);
                return;
            }

            Evenement e = createEventFromFields(selected.getId_utilisateur());
            e.setId_evenement(selected.getId_evenement());
            try {
                evenementCRUD.modifier(e);
                loadEvenements();
                clearForm();
                showAlert("Modification reussie", "L'evenement a ete modifie avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleAnnulerModification() {
        clearForm();
    }

    @FXML
    private void handleSupprimer() {
        Evenement selected = getSelectedEvent();
        if (selected == null) {
            showAlert("Selection requise", "Veuillez selectionner un evenement dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        if (!canManageEvent(selected)) {
            showAlert("Acces refuse", "Seul l'admin peut supprimer un evenement.", Alert.AlertType.WARNING);
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
            loadEvenements();
            clearForm();
            showAlert("Suppression reussie", "L'evenement a ete supprime.", Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Evenement createEventFromFields() {
        return createEventFromFields(SessionManager.getCurrentUserId());
    }

    private Evenement createEventFromFields(long ownerId) {
        Evenement e = new Evenement();
        e.setNom(txtNom.getText().trim());
        e.setDate_evenement(Date.valueOf(datePicker.getValue()));
        e.setLieu(txtLieu.getText().trim());
        e.setDuree_minutes(Integer.parseInt(txtDuree.getText().trim()));
        e.setImage(txtImage.getText().trim());
        e.setId_utilisateur(ownerId);
        return e;
    }

    private void clearForm() {
        clearFormFields();
        listEvenements.getSelectionModel().clearSelection();
        applyRolePermissions(null);
        showEventListPage();
    }

    private void clearFormFields() {
        txtNom.clear();
        datePicker.setValue(null);
        txtLieu.clear();
        txtDuree.clear();
        txtImage.clear();
        showEmptyImagePreview();
        resetValidationStyles();
    }

    private boolean validateInput() {
        resetValidationStyles();
        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        String nom = txtNom.getText() == null ? "" : txtNom.getText().trim();
        String lieu = txtLieu.getText() == null ? "" : txtLieu.getText().trim();
        String dureeText = txtDuree.getText() == null ? "" : txtDuree.getText().trim();
        String image = txtImage.getText() == null ? "" : txtImage.getText().trim();

        if (nom.isEmpty()) {
            setInvalid(txtNom);
            errors.append("- Le nom de l'evenement est obligatoire.\n");
            valid = false;
        } else if (nom.length() < 3 || nom.length() > 50) {
            setInvalid(txtNom);
            errors.append("- Le nom doit contenir entre 3 et 50 caracteres.\n");
            valid = false;
        }

        if (datePicker.getValue() == null) {
            setInvalid(datePicker);
            errors.append("- La date de l'evenement est obligatoire.\n");
            valid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            setInvalid(datePicker);
            errors.append("- La date de l'evenement ne doit pas etre avant aujourd'hui.\n");
            valid = false;
        }

        if (lieu.isEmpty()) {
            setInvalid(txtLieu);
            errors.append("- Le lieu est obligatoire.\n");
            valid = false;
        } else if (lieu.length() < 3 || lieu.length() > 50) {
            setInvalid(txtLieu);
            errors.append("- Le lieu doit contenir entre 3 et 50 caracteres.\n");
            valid = false;
        }

        if (dureeText.isEmpty()) {
            setInvalid(txtDuree);
            errors.append("- La duree est obligatoire.\n");
            valid = false;
        } else {
            try {
                int duree = Integer.parseInt(dureeText);
                if (duree < 15 || duree > 480) {
                    setInvalid(txtDuree);
                    errors.append("- La duree doit etre entre 15 et 480 minutes.\n");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setInvalid(txtDuree);
                errors.append("- La duree doit etre un nombre entier en minutes.\n");
                valid = false;
            }
        }

        if (!image.isEmpty() && !isImageSourceValid(image)) {
            setInvalid(txtImage);
            errors.append("- L'image doit etre une URL valide, un fichier existant ou une ressource classpath.\n");
            valid = false;
        }

        if (SessionManager.getCurrentUserId() <= 0) {
            errors.append("- Aucun utilisateur connecte.\n");
            valid = false;
        }

        if (!valid) {
            showAlert("Controle de saisie", errors.toString(), Alert.AlertType.WARNING);
        }

        return valid;
    }

    private boolean isImageSourceValid(String source) {
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

    private void setInvalid(Control c) {
        c.getStyleClass().add("field-error");
    }

    private void resetValidationStyles() {
        txtNom.getStyleClass().remove("field-error");
        datePicker.getStyleClass().remove("field-error");
        txtLieu.getStyleClass().remove("field-error");
        txtDuree.getStyleClass().remove("field-error");
        txtImage.getStyleClass().remove("field-error");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

