package com.skillora.controllers;

import entities.Evenement;
import entities.Role;
import entities.Reservation;
import services.EvenementCRUD;
import services.ReservationCRUD;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class ReservationController implements Initializable {

    @FXML private Label lblNbPlaces;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtRechercheReservation;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private TextField txtEvenementSelectionne;
    @FXML private Label lblCurrentUser;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnulerModification;
    @FXML private Button btnOpenReservationForm;
    @FXML private Button btnOpenSelectedReservationForm;
    @FXML private Button btnListSupprimer;
    @FXML private Button btnListAccepter;
    @FXML private Button btnListRefuser;
    @FXML private VBox reservationListPage;
    @FXML private VBox reservationFormPage;
    @FXML private VBox actionBox;
    @FXML private HBox rowEditActions;
    @FXML private VBox eventPreviewCard;
    @FXML private ImageView eventPreviewImage;
    @FXML private Label lblPreviewTitle;
    @FXML private Label lblPreviewDate;
    @FXML private Label lblPreviewLieu;
    @FXML private Label lblPreviewAction;
    
    @FXML private ListView<Reservation> listReservations;
    @FXML private ScrollPane reservationGridScroll;
    @FXML private TilePane reservationGridView;
    @FXML private ListView<Evenement> listChoixEvenements;
    @FXML private ScrollPane eventGridScroll;
    @FXML private TilePane eventGridView;
    @FXML private GridPane seatGrid;
    @FXML private Label lblSelectedSeats;

    private final ReservationCRUD reservationCRUD = new ReservationCRUD();
    private final EvenementCRUD evenementCRUD = new EvenementCRUD();
    private ObservableList<Reservation> allReservationList = FXCollections.observableArrayList();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private ObservableList<Evenement> eventChoiceList = FXCollections.observableArrayList();
    private int nbPlaces = 1;
    private LocalDate maxReservationDate;
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private Set<String> occupiedSeats = new LinkedHashSet<>();
    private boolean populatingReservation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupEventChoiceList();
        setupDatePicker();
        setupReservationList();
        setupReservationFilters();
        setupSeatGrid();
        setupCurrentUser();
        loadEventChoices();
        loadReservations();
        showReservationListPage();
    }

    private void setupDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean afterSelectedEvent = maxReservationDate != null && date.isAfter(maxReservationDate);
                setDisable(empty || date.isBefore(LocalDate.now()) || afterSelectedEvent);
            }
        });
    }

    private void setupCurrentUser() {
        lblCurrentUser.setText(SessionManager.getCurrentUserName()
                + " | Role: " + SessionManager.getCurrentUserRoleLabel());
        applyRolePermissions(null);
    }

    private void setupReservationList() {
        listReservations.setCellFactory(list -> createReservationListCell());
        listReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
            applyRolePermissions(newSelection);
        });
    }

    private void setupReservationFilters() {
        comboFiltreStatut.getItems().setAll("Tous", "En attente", "Acceptee", "Refusee");
        comboFiltreStatut.setValue("Tous");
        txtRechercheReservation.textProperty().addListener((observable, oldValue, newValue) -> applyReservationFilters());
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> applyReservationFilters());
    }

    private void applyRolePermissions(Reservation selectedReservation) {
        Role role = SessionManager.getCurrentUserRole();
        boolean canCreate = canCreateReservation();
        boolean canModifySelected = canModifyReservation(selectedReservation);
        boolean canDeleteSelected = canDeleteReservation(selectedReservation);
        boolean adminCanDecide = role == Role.ADMIN && selectedReservation != null;
        boolean student = role == Role.ETUDIANT;
        boolean admin = role == Role.ADMIN;
        boolean hasActionsForRole = student || admin;
        boolean hasSelection = selectedReservation != null;

        setNodeVisible(eventPreviewCard, student || canModifySelected);
        lblNbPlaces.setDisable(!canCreate && !canModifySelected);
        datePicker.setDisable(!canCreate && !canModifySelected);
        txtEvenementSelectionne.setDisable(!canCreate && !canModifySelected);
        listChoixEvenements.setDisable(!canCreate && !canModifySelected);
        eventGridScroll.setDisable(true);
        setNodeVisible(listChoixEvenements, true);
        setNodeVisible(eventGridScroll, false);
        setNodeVisible(listReservations, !student);
        setNodeVisible(reservationGridScroll, student);

        setNodeVisible(btnOpenReservationForm, student);
        setNodeVisible(btnOpenSelectedReservationForm, student || admin);
        setNodeVisible(btnListSupprimer, student || admin);
        setNodeVisible(btnListAccepter, admin);
        setNodeVisible(btnListRefuser, admin);
        btnOpenReservationForm.setDisable(!canCreate);
        btnOpenSelectedReservationForm.setDisable(!canModifySelected);
        btnListSupprimer.setDisable(!canDeleteSelected);
        btnListAccepter.setDisable(!adminCanDecide);
        btnListRefuser.setDisable(!adminCanDecide);

        setNodeVisible(actionBox, hasActionsForRole);
        setNodeVisible(btnAjouter, student && !hasSelection);
        setNodeVisible(rowEditActions, (student || admin) && hasSelection);
        setNodeVisible(btnModifier, student || admin);
        setNodeVisible(btnAnnulerModification, student || admin);

        btnAjouter.setDisable(!canCreate);
        btnModifier.setDisable(!canModifySelected);
        btnAnnulerModification.setDisable(!hasSelection);
    }

    private void setNodeVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void setupEventChoiceList() {
        listChoixEvenements.setCellFactory(list -> createEventChoiceListCell());
        listChoixEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtEvenementSelectionne.setText(newSelection.getNom());
                maxReservationDate = newSelection.getDate_evenement().toLocalDate();
                if (datePicker.getValue() != null && datePicker.getValue().isAfter(maxReservationDate)) {
                    datePicker.setValue(null);
                }
                txtEvenementSelectionne.getStyleClass().remove("field-error");
                updateEventPreview(newSelection);
                if (!populatingReservation) {
                    selectedSeats.clear();
                }
                refreshSeatGrid();
                renderEventGrid();
            } else {
                maxReservationDate = null;
                txtEvenementSelectionne.clear();
                updateEventPreview(null);
                selectedSeats.clear();
                refreshSeatGrid();
                renderEventGrid();
            }
        });
    }

    private void renderEventGrid() {
        eventGridView.getChildren().clear();
        Evenement selectedEvent = getSelectedEvent();

        for (Evenement evenement : eventChoiceList) {
            VBox card = createEventGridCard(evenement, selectedEvent);
            eventGridView.getChildren().add(card);
        }
    }

    private VBox createEventGridCard(Evenement evenement, Evenement selectedEvent) {
        VBox card = new VBox(6);
        card.setPrefSize(130, 152);
        card.getStyleClass().add("event-grid-card");

        if (selectedEvent != null && selectedEvent.getId_evenement() == evenement.getId_evenement()) {
            card.getStyleClass().add("event-grid-card-selected");
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(112);
        imageView.setFitHeight(58);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("event-grid-image");
        if (evenement.getImage() != null && !evenement.getImage().trim().isEmpty()) {
            try {
                Image image = new Image(resolveImageSource(evenement.getImage()), 112, 58, true, true, true);
                imageView.setImage(image.isError() ? null : image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        Label title = new Label(evenement.getNom());
        title.setWrapText(true);
        title.setMaxWidth(112);
        title.getStyleClass().add("event-grid-title");

        Label date = new Label(String.valueOf(evenement.getDate_evenement()));
        date.getStyleClass().add("event-grid-meta");

        Label place = new Label(evenement.getLieu());
        place.setWrapText(true);
        place.setMaxWidth(112);
        place.getStyleClass().add("event-grid-meta");

        card.getChildren().addAll(imageView, title, date, place);
        card.setOnMouseClicked(event -> selectEventChoice(evenement.getId_evenement()));

        return card;
    }

    private void setupSeatGrid() {
        refreshSeatGrid();
    }

    private void refreshSeatGrid() {
        seatGrid.getChildren().clear();
        occupiedSeats = loadOccupiedSeatsForSelectedEvent();

        String[] rows = {"A", "B", "C", "D", "E", "F"};
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < 5; col++) {
                String seatCode = rows[row] + (col + 1);
                Button seatButton = new Button(seatCode);
                seatButton.setPrefSize(36, 28);
                seatButton.setMinSize(36, 28);
                seatButton.getStyleClass().add("seat-btn");

                boolean occupied = occupiedSeats.contains(seatCode);
                boolean selected = selectedSeats.contains(seatCode);
                if (occupied && !selected) {
                    seatButton.getStyleClass().add("seat-occupied");
                    seatButton.setDisable(true);
                } else if (selected) {
                    seatButton.getStyleClass().add("seat-selected");
                }

                seatButton.setOnAction(event -> toggleSeat(seatCode));
                seatGrid.add(seatButton, col, row);
            }
        }

        updateSelectedSeatsLabel();
    }

    private Set<String> loadOccupiedSeatsForSelectedEvent() {
        Set<String> seats = new LinkedHashSet<>();
        Evenement selectedEvent = getSelectedEvent();
        if (selectedEvent == null) {
            return seats;
        }

        Reservation selectedReservation = getSelectedReservation();
        int excludedReservationId = selectedReservation != null ? selectedReservation.getId_reservation() : 0;

        try {
            seats.addAll(reservationCRUD.getChaisesReservees(selectedEvent.getId_evenement(), excludedReservationId));
        } catch (SQLException e) {
            showAlert("Erreur chaises", "Impossible de charger les chaises deja reservees.", Alert.AlertType.ERROR);
        }

        return seats;
    }

    private void toggleSeat(String seatCode) {
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
        setNbPlaces(selectedSeats.size());
        lblSelectedSeats.getStyleClass().remove("field-error");
        refreshSeatGrid();
    }

    private void loadEventChoices() {
        try {
            eventChoiceList.setAll(evenementCRUD.afficher());
            listChoixEvenements.setItems(eventChoiceList);
            renderEventGrid();
        } catch (Exception e) {
            showAlert("Erreur de connexion", "Impossible de charger la liste des evenements.", Alert.AlertType.ERROR);
        }
    }

    private boolean canCreateReservation() {
        Role role = SessionManager.getCurrentUserRole();
        return role == Role.ETUDIANT;
    }

    private boolean canModifyReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        return role == Role.ADMIN
                || (role == Role.ETUDIANT
                && reservation.getId_utilisateur() == currentUserId
                && !"ACCEPTEE".equals(reservation.getStatut()));
    }

    private boolean canDeleteReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        return role == Role.ADMIN
                || (role == Role.ETUDIANT && reservation.getId_utilisateur() == currentUserId);
    }

    private ListCell<Reservation> createReservationListCell() {
        return new ListCell<Reservation>() {
            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);
                if (empty || reservation == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("reservation-list-row");

                ImageView imageView = new ImageView();
                imageView.setFitWidth(82);
                imageView.setFitHeight(54);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("reservation-list-image");
                if (reservation.getImage_evenement() != null && !reservation.getImage_evenement().trim().isEmpty()) {
                    try {
                        Image image = new Image(resolveImageSource(reservation.getImage_evenement()), 82, 54, true, true, true);
                        imageView.setImage(image.isError() ? null : image);
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }

                VBox info = new VBox(4);
                info.setPrefWidth(460);

                Label title = new Label(formatSeats(reservation.getNom_evenement()));
                title.setWrapText(true);
                title.getStyleClass().add("reservation-list-title");

                Label details = new Label("Etudiant: " + formatUserName(reservation)
                        + " | Date: " + reservation.getDate_reservation()
                        + " | Places: " + reservation.getNb_places()
                        + " | Chaises: " + formatSeats(reservation.getChaises()));
                details.setWrapText(true);
                details.getStyleClass().add("reservation-list-meta");

                info.getChildren().addAll(title, details);

                Label status = new Label(formatStatut(reservation.getStatut()));
                status.setMinWidth(92);
                status.setAlignment(Pos.CENTER);
                status.getStyleClass().add("reservation-list-status");
                status.getStyleClass().add(statusStyleClass(reservation.getStatut()));

                row.getChildren().addAll(imageView, info, status);
                setText(null);
                setGraphic(row);
            }
        };
    }

    private ListCell<Evenement> createEventChoiceListCell() {
        return new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    VBox content = new VBox(3);
                    Label title = new Label(evenement.getNom());
                    title.setWrapText(true);
                    title.setMaxWidth(300);
                    title.getStyleClass().add("event-choice-title");

                    Label meta = new Label(evenement.getDate_evenement() + " | " + evenement.getLieu());
                    meta.setWrapText(true);
                    meta.setMaxWidth(300);
                    meta.getStyleClass().add("event-choice-meta");

                    content.getChildren().addAll(title, meta);
                    setText(null);
                    setGraphic(content);
                    setTooltip(new Tooltip(evenement.getNom()));
                }
            }
        };
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

    private String formatStatut(String statut) {
        if ("ACCEPTEE".equals(statut)) {
            return "Acceptee";
        }
        if ("REFUSEE".equals(statut)) {
            return "Refusee";
        }
        return "En attente";
    }

    private String statusStyleClass(String statut) {
        if ("ACCEPTEE".equals(statut)) {
            return "status-accepted";
        }
        if ("REFUSEE".equals(statut)) {
            return "status-refused";
        }
        return "status-pending";
    }

    private String formatSeats(String seats) {
        if (seats == null || seats.trim().isEmpty()) {
            return "-";
        }
        return seats;
    }

    private String formatUserName(Reservation reservation) {
        if (reservation.getNom_utilisateur() != null && !reservation.getNom_utilisateur().trim().isEmpty()) {
            return reservation.getNom_utilisateur();
        }
        return "Utilisateur #" + reservation.getId_utilisateur();
    }

    private Reservation getSelectedReservation() {
        return listReservations.getSelectionModel().getSelectedItem();
    }

    private void populateFields(Reservation r) {
        populatingReservation = true;
        try {
            setNbPlaces(r.getNb_places());
            datePicker.setValue(r.getDate_reservation().toLocalDate());
            txtEvenementSelectionne.setText(r.getNom_evenement());
            selectedSeats.clear();
            selectedSeats.addAll(parseSeats(r.getChaises()));
            selectEventChoice(r.getId_evenement());
            refreshSeatGrid();
            resetValidationStyles();
        } finally {
            populatingReservation = false;
        }
    }

    private void selectEventChoice(int idEvenement) {
        for (Evenement evenement : eventChoiceList) {
            if (evenement.getId_evenement() == idEvenement) {
                listChoixEvenements.getSelectionModel().select(evenement);
                listChoixEvenements.scrollTo(evenement);
                return;
            }
        }
        listChoixEvenements.getSelectionModel().clearSelection();
    }

    private void loadReservations() {
        try {
            List<Reservation> data = filterReservationsForCurrentRole(reservationCRUD.afficher());
            allReservationList.setAll(data);
            applyReservationFilters();
        } catch (Exception e) {
            showAlert("Erreur de connexion", "Impossible de contacter la base de donnees. Assurez-vous que MySQL est lance sur le port 3306.", Alert.AlertType.ERROR);
        }
    }

    private void applyReservationFilters() {
        String search = txtRechercheReservation.getText() != null
                ? txtRechercheReservation.getText().trim().toLowerCase()
                : "";
        String statusFilter = comboFiltreStatut.getValue() != null ? comboFiltreStatut.getValue() : "Tous";

        List<Reservation> filteredReservations = new ArrayList<>();
        for (Reservation reservation : allReservationList) {
            if (!matchesStatusFilter(reservation, statusFilter)) {
                continue;
            }
            if (!matchesSearchFilter(reservation, search)) {
                continue;
            }
            filteredReservations.add(reservation);
        }

        reservationList.setAll(filteredReservations);
        listReservations.setItems(reservationList);
        renderReservationGrid();
        applyRolePermissions(getSelectedReservation());
    }

    private boolean matchesStatusFilter(Reservation reservation, String statusFilter) {
        if ("Tous".equals(statusFilter)) {
            return true;
        }
        return formatStatut(reservation.getStatut()).equals(statusFilter);
    }

    private boolean matchesSearchFilter(Reservation reservation, String search) {
        if (search.isEmpty()) {
            return true;
        }

        String content = (formatSeats(reservation.getNom_evenement()) + " "
                + formatUserName(reservation) + " "
                + formatSeats(reservation.getChaises()) + " "
                + reservation.getDate_reservation() + " "
                + formatStatut(reservation.getStatut())).toLowerCase();
        return content.contains(search);
    }

    private void renderReservationGrid() {
        reservationGridView.getChildren().clear();

        for (Reservation reservation : reservationList) {
            VBox card = createReservationGridCard(reservation);
            reservationGridView.getChildren().add(card);
        }
    }

    private VBox createReservationGridCard(Reservation reservation) {
        VBox card = new VBox(8);
        card.setPrefSize(245, 210);
        card.getStyleClass().add("reservation-grid-card");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(82);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("reservation-grid-image");
        if (reservation.getImage_evenement() != null && !reservation.getImage_evenement().trim().isEmpty()) {
            try {
                Image image = new Image(resolveImageSource(reservation.getImage_evenement()), 220, 82, true, true, true);
                imageView.setImage(image.isError() ? null : image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        Label title = new Label(formatSeats(reservation.getNom_evenement()));
        title.setWrapText(true);
        title.setMaxWidth(220);
        title.getStyleClass().add("reservation-grid-title");

        Label date = new Label("Date: " + reservation.getDate_reservation());
        date.getStyleClass().add("reservation-grid-meta");

        Label seats = new Label("Chaises: " + formatSeats(reservation.getChaises()));
        seats.setWrapText(true);
        seats.setMaxWidth(220);
        seats.getStyleClass().add("reservation-grid-meta");

        Label status = new Label(formatStatut(reservation.getStatut()));
        status.getStyleClass().add("reservation-grid-status");
        status.getStyleClass().add(statusStyleClass(reservation.getStatut()));

        card.getChildren().addAll(imageView, title, date, seats, status);
        card.setOnMouseClicked(event -> {
            listReservations.getSelectionModel().select(reservation);
            applyRolePermissions(reservation);
        });

        return card;
    }

    @FXML
    private void handleOpenAddForm(ActionEvent event) {
        listReservations.getSelectionModel().clearSelection();
        clearFormFields();
        applyRolePermissions(null);
        showReservationFormPage();
    }

    @FXML
    private void handleOpenSelectedForm(ActionEvent event) {
        Reservation selected = getSelectedReservation();
        if (selected == null) {
            showAlert("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        populateFields(selected);
        applyRolePermissions(selected);
        showReservationFormPage();
    }

    @FXML
    private void handleBackToReservationList(ActionEvent event) {
        showReservationListPage();
    }

    private void showReservationListPage() {
        setNodeVisible(reservationListPage, true);
        setNodeVisible(reservationFormPage, false);
    }

    private void showReservationFormPage() {
        setNodeVisible(reservationListPage, false);
        setNodeVisible(reservationFormPage, true);
    }

    private List<Reservation> filterReservationsForCurrentRole(List<Reservation> reservations) {
        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        if (role == Role.ADMIN) {
            return reservations;
        }

        List<Reservation> filteredReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (role == Role.ETUDIANT && reservation.getId_utilisateur() == currentUserId) {
                filteredReservations.add(reservation);
            } else if (role == Role.INSTRUCTEUR
                    && reservation.getId_organisateur_evenement() == currentUserId) {
                filteredReservations.add(reservation);
            }
        }
        return filteredReservations;
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (!canCreateReservation()) {
            showAlert("Acces refuse", "Seul l'etudiant peut ajouter une reservation.", Alert.AlertType.WARNING);
            return;
        }

        if (validateInput()) {
            Reservation r = createReservationFromFields();
            try {
                reservationCRUD.ajouter(r);
                loadReservations();
                clearForm();
                showAlert("Reservation envoyee", "Reservation envoyee, en attente de validation admin.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Reservation selected = getSelectedReservation();
        if (!canModifyReservation(selected)) {
            if (selected != null
                    && SessionManager.getCurrentUserRole() == Role.ETUDIANT
                    && "ACCEPTEE".equals(selected.getStatut())) {
                showAlert("Modification impossible", "Cette reservation est deja acceptee. Vous ne pouvez plus la modifier.", Alert.AlertType.WARNING);
            } else {
                showAlert("Acces refuse", "Vous ne pouvez modifier que vos propres reservations.", Alert.AlertType.WARNING);
            }
            return;
        }

        if (validateInput()) {
            if (isSelectedReservationUnchanged(selected)) {
                showAlert("Aucune modification", "Aucune modification detectee.", Alert.AlertType.INFORMATION);
                return;
            }

            Reservation r = createReservationFromFields(selected.getId_utilisateur());
            r.setId_reservation(selected.getId_reservation());
            try {
                reservationCRUD.modifier(r);
                loadReservations();
                clearForm();
                showAlert("Modification reussie", "La reservation a ete modifiee avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean isSelectedReservationUnchanged(Reservation selected) {
        if (selected == null || datePicker.getValue() == null || getSelectedEvent() == null) {
            return false;
        }

        String selectedChaises = selected.getChaises() != null ? selected.getChaises().trim() : "";
        String formChaises = String.join(",", selectedSeats);

        return selected.getNb_places() == selectedSeats.size()
                && selected.getDate_reservation().toLocalDate().equals(datePicker.getValue())
                && selected.getId_evenement() == getSelectedEvent().getId_evenement()
                && selectedChaises.equals(formChaises);
    }

    @FXML
    private void handleAnnulerModification(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Reservation selected = getSelectedReservation();
        if (selected == null) {
            showAlert("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        if (!canDeleteReservation(selected)) {
            showAlert("Acces refuse", "Vous ne pouvez supprimer que les reservations autorisees pour votre role.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la reservation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette reservation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            reservationCRUD.supprimer(selected.getId_reservation());
            loadReservations();
            clearForm();
            showAlert("Suppression reussie", "La reservation a ete supprimee.", Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAccepter(ActionEvent event) {
        changerStatutReservation(
                "ACCEPTEE",
                "Reservation acceptee.",
                "Confirmer acceptation",
                "Voulez-vous vraiment accepter cette reservation ?"
        );
    }

    @FXML
    private void handleRefuser(ActionEvent event) {
        changerStatutReservation(
                "REFUSEE",
                "Reservation refusee.",
                "Confirmer refus",
                "Voulez-vous vraiment refuser cette reservation ?"
        );
    }

    private void changerStatutReservation(String statut, String successMessage, String confirmTitle, String confirmMessage) {
        Reservation selected = getSelectedReservation();
        if (SessionManager.getCurrentUserRole() != Role.ADMIN) {
            showAlert("Acces refuse", "Seul l'admin peut accepter ou refuser une reservation.", Alert.AlertType.WARNING);
            return;
        }
        if (selected == null) {
            showAlert("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        if (!confirmAction(confirmTitle, confirmMessage)) {
            return;
        }

        try {
            reservationCRUD.changerStatut(selected.getId_reservation(), statut);
            loadReservations();
            clearForm();
            showAlert("Statut mis a jour", successMessage, Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    @FXML
    private void handleDiminuerPlaces(ActionEvent event) {
        setNbPlaces(nbPlaces - 1);
    }

    @FXML
    private void handleAugmenterPlaces(ActionEvent event) {
        setNbPlaces(nbPlaces + 1);
    }

    private void setNbPlaces(int value) {
        nbPlaces = Math.max(0, value);
        lblNbPlaces.setText(String.valueOf(nbPlaces));
        lblNbPlaces.getStyleClass().remove("field-error");
    }

    private Reservation createReservationFromFields() {
        return createReservationFromFields(SessionManager.getCurrentUserId());
    }

    private Reservation createReservationFromFields(long userId) {
        Reservation r = new Reservation();
        r.setNb_places(selectedSeats.size());
        r.setDate_reservation(Date.valueOf(datePicker.getValue()));
        r.setId_evenement(getSelectedEvent().getId_evenement());
        r.setId_utilisateur(userId);
        r.setChaises(String.join(",", selectedSeats));
        return r;
    }

    private void clearForm() {
        clearFormFields();
        listReservations.getSelectionModel().clearSelection();
        applyRolePermissions(null);
        showReservationListPage();
    }

    private void clearFormFields() {
        setNbPlaces(1);
        datePicker.setValue(null);
        txtEvenementSelectionne.clear();
        maxReservationDate = null;
        selectedSeats.clear();
        updateEventPreview(null);
        listChoixEvenements.getSelectionModel().clearSelection();
        refreshSeatGrid();
        resetValidationStyles();
    }

    private void updateEventPreview(Evenement evenement) {
        if (evenement == null) {
            eventPreviewImage.setImage(null);
            lblPreviewTitle.setText("Choisissez un evenement");
            lblPreviewDate.setText("Date : -");
            lblPreviewLieu.setText("Lieu : -");
            lblPreviewAction.setText("Selectionnez un evenement dans la liste pour voir son apercu.");
            return;
        }

        lblPreviewTitle.setText(evenement.getNom());
        lblPreviewDate.setText("Date : " + evenement.getDate_evenement());
        lblPreviewLieu.setText("Lieu : " + evenement.getLieu());
        lblPreviewAction.setText("Pret a reserver : choisissez la date puis confirmez votre demande.");

        String imagePath = evenement.getImage();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            eventPreviewImage.setImage(null);
            return;
        }

        try {
            Image image = new Image(resolveImageSource(imagePath), 300, 180, true, true, true);
            eventPreviewImage.setImage(image.isError() ? null : image);
        } catch (Exception e) {
            eventPreviewImage.setImage(null);
        }
    }

    private boolean validateInput() {
        resetValidationStyles();
        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        if (selectedSeats.isEmpty()) {
            setInvalid(lblSelectedSeats);
            errors.append("- Vous devez choisir au moins une chaise.\n");
            valid = false;
        }
        LocalDate reservationDate = datePicker.getValue();
        Evenement selectedEvent = getSelectedEvent();

        if (reservationDate == null) {
            setInvalid(datePicker);
            errors.append("- La date de reservation est obligatoire.\n");
            valid = false;
        } else if (reservationDate.isBefore(LocalDate.now())) {
            setInvalid(datePicker);
            errors.append("- La date de reservation ne doit pas etre avant aujourd'hui.\n");
            valid = false;
        }
        if (selectedEvent == null) {
            setInvalid(txtEvenementSelectionne);
            errors.append("- Vous devez choisir un evenement dans la liste.\n");
            valid = false;
        } else {
            LocalDate eventDate = selectedEvent.getDate_evenement().toLocalDate();
            if (eventDate.isBefore(LocalDate.now())) {
                setInvalid(txtEvenementSelectionne);
                errors.append("- Cet evenement est deja passe, il ne peut plus etre reserve.\n");
                valid = false;
            } else if (reservationDate != null && reservationDate.isAfter(eventDate)) {
                setInvalid(datePicker);
                errors.append("- La date de reservation ne doit pas depasser la date de l'evenement.\n");
                valid = false;
            }
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

    private Set<String> parseSeats(String seats) {
        Set<String> parsedSeats = new LinkedHashSet<>();
        if (seats == null || seats.trim().isEmpty()) {
            return parsedSeats;
        }

        for (String seat : seats.split(",")) {
            String code = seat.trim();
            if (!code.isEmpty()) {
                parsedSeats.add(code);
            }
        }

        return parsedSeats;
    }

    private void updateSelectedSeatsLabel() {
        if (selectedSeats.isEmpty()) {
            lblSelectedSeats.setText("Aucune chaise choisie");
            setNbPlaces(0);
            return;
        }

        lblSelectedSeats.setText("Chaises: " + String.join(", ", selectedSeats));
        setNbPlaces(selectedSeats.size());
    }

    private Evenement getSelectedEvent() {
        Evenement selected = listChoixEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        return null;
    }

    private void setInvalid(Control c) {
        c.getStyleClass().add("field-error");
    }

    private void resetValidationStyles() {
        lblNbPlaces.getStyleClass().remove("field-error");
        lblSelectedSeats.getStyleClass().remove("field-error");
        datePicker.getStyleClass().remove("field-error");
        txtEvenementSelectionne.getStyleClass().remove("field-error");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

