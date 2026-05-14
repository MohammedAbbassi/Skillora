package com.skillora.events.controllers;

import entities.Evenement;
import entities.Role;
import entities.Reservation;
import services.EvenementCRUD;
import services.ReservationCRUD;
import services.WeatherService;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    @FXML private Button btnToggleMyReservations;
    @FXML private Label lblReservationPageTitle;
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
    @FXML private ScrollPane availableEventScroll;
    @FXML private TilePane availableEventGrid;
    @FXML private ListView<Evenement> listChoixEvenements;
    @FXML private ScrollPane eventGridScroll;
    @FXML private TilePane eventGridView;
    @FXML private GridPane seatGrid;
    @FXML private Label lblSelectedSeats;

    private final ReservationCRUD reservationCRUD = new ReservationCRUD();
    private final EvenementCRUD evenementCRUD = new EvenementCRUD();
    private final WeatherService weatherService = new WeatherService();
    private ObservableList<Reservation> allReservationList = FXCollections.observableArrayList();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private ObservableList<Evenement> allAvailableEventList = FXCollections.observableArrayList();
    private ObservableList<Evenement> eventChoiceList = FXCollections.observableArrayList();
    private int nbPlaces = 1;
    private LocalDate maxReservationDate;
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private Set<String> occupiedSeats = new LinkedHashSet<>();
    private boolean populatingReservation;
    private boolean afficherMesReservations;
    private boolean fixedEmbeddedMode;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preparerListeChoixEvenements();
        preparerCalendrier();
        preparerListeReservations();
        preparerFiltresReservations();
        preparerPlanChaises();
        afficherUtilisateurConnecte();
        chargerChoixEvenementsDepuisBase();
        chargerReservationsDepuisBase();
        afficherListeReservations();
    }

    public void showAvailableEventsOnly() {
        setEmbeddedMode(false);
    }

    public void showMyReservationsOnly() {
        setEmbeddedMode(true);
    }

    private void setEmbeddedMode(boolean showMyReservations) {
        fixedEmbeddedMode = true;
        afficherMesReservations = showMyReservations;
        listReservations.getSelectionModel().clearSelection();
        txtRechercheReservation.clear();
        comboFiltreStatut.setValue("Tous");
        appliquerFiltresEcranPrincipal();
        appliquerPermissionsSelonRole(null);
        afficherListeReservations();
    }

    private void preparerCalendrier() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean afterSelectedEvent = maxReservationDate != null && date.isAfter(maxReservationDate);
                setDisable(empty || date.isBefore(LocalDate.now()) || afterSelectedEvent);
            }
        });
    }

    private void afficherUtilisateurConnecte() {
        lblCurrentUser.setText(SessionManager.getCurrentUserName()
                + " | Role: " + SessionManager.getCurrentUserRoleLabel());
        appliquerPermissionsSelonRole(null);
    }

    private void preparerListeReservations() {
        listReservations.setCellFactory(list -> creerLigneListeReservation());
        listReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
            appliquerPermissionsSelonRole(newSelection);
        });
    }

    private void preparerFiltresReservations() {
        comboFiltreStatut.getItems().setAll("Tous", "En attente", "Acceptee", "Refusee");
        comboFiltreStatut.setValue("Tous");
        txtRechercheReservation.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltresEcranPrincipal());
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltresEcranPrincipal());
    }

    private void appliquerFiltresEcranPrincipal() {
        if (utiliseInterfaceEtudiant(SessionManager.getCurrentUserRole()) && !afficherMesReservations) {
            appliquerFiltresEvenementsDisponibles();
        } else {
            appliquerFiltresReservations();
        }
    }

    private void appliquerPermissionsSelonRole(Reservation selectedReservation) {
        Role role = SessionManager.getCurrentUserRole();
        boolean canCreate = peutCreerReservation();
        boolean canModifySelected = peutModifierReservation(selectedReservation);
        boolean canDeleteSelected = peutSupprimerReservation(selectedReservation);
        boolean student = utiliseInterfaceEtudiant(role);
        boolean admin = role == Role.ADMIN;
        boolean canDecideSelected = peutDeciderReservation(selectedReservation);
        boolean hasActionsForRole = student || admin;
        boolean hasSelection = selectedReservation != null;

        if (student && !afficherMesReservations) {
            lblReservationPageTitle.setText("Liste des Evenements Disponibles");
        } else if (student) {
            lblReservationPageTitle.setText("Mes Reservations");
        } else {
            lblReservationPageTitle.setText("Liste des Reservations");
        }
        txtRechercheReservation.setPromptText(student
                ? (afficherMesReservations
                ? "Rechercher evenement, chaise, statut..."
                : "Rechercher evenement, lieu, date, duree...")
                : "Rechercher evenement, etudiant, chaise...");
        rendreElementVisible(comboFiltreStatut, !student);
        rendreElementVisible(eventPreviewCard, false);
        lblNbPlaces.setDisable(!canCreate && !canModifySelected);
        datePicker.setDisable(!canCreate && !canModifySelected);
        txtEvenementSelectionne.setDisable(!canCreate && !canModifySelected);
        listChoixEvenements.setDisable(!canCreate && !canModifySelected);
        eventGridScroll.setDisable(!canCreate && !canModifySelected);
        rendreElementVisible(listChoixEvenements, false);
        rendreElementVisible(eventGridScroll, false);
        rendreElementVisible(listReservations, !student);
        rendreElementVisible(reservationGridScroll, student && afficherMesReservations);
        rendreElementVisible(availableEventScroll, student && !afficherMesReservations);

        rendreElementVisible(btnOpenReservationForm, false);
        rendreElementVisible(btnOpenSelectedReservationForm, admin);
        rendreElementVisible(btnListSupprimer, admin);
        rendreElementVisible(btnListAccepter, admin);
        rendreElementVisible(btnListRefuser, admin);
        rendreElementVisible(btnToggleMyReservations, student && !fixedEmbeddedMode);
        btnToggleMyReservations.setText(afficherMesReservations ? "Voir les evenements" : "Consulter mes reservations");
        btnOpenReservationForm.setDisable(!canCreate);
        btnOpenSelectedReservationForm.setDisable(!canModifySelected);
        btnListSupprimer.setDisable(!canDeleteSelected);
        btnListAccepter.setDisable(!canDecideSelected);
        btnListRefuser.setDisable(!canDecideSelected);

        rendreElementVisible(actionBox, hasActionsForRole);
        rendreElementVisible(btnAjouter, student && !hasSelection);
        rendreElementVisible(rowEditActions, (student || admin) && hasSelection);
        rendreElementVisible(btnModifier, student || admin);
        rendreElementVisible(btnAnnulerModification, student || admin);

        btnAjouter.setDisable(!canCreate);
        btnModifier.setDisable(!canModifySelected);
        btnAnnulerModification.setDisable(!hasSelection);
    }

    private void rendreElementVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void preparerListeChoixEvenements() {
        listChoixEvenements.setCellFactory(list -> creerLigneChoixEvenement());
        listChoixEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtEvenementSelectionne.setText(newSelection.getNom());
                maxReservationDate = newSelection.getDate_evenement().toLocalDate();
                if (datePicker.getValue() != null && datePicker.getValue().isAfter(maxReservationDate)) {
                    datePicker.setValue(null);
                }
                txtEvenementSelectionne.getStyleClass().remove("field-error");
                mettreAJourApercuEvenement(newSelection);
                if (!populatingReservation) {
                    selectedSeats.clear();
                }
                rafraichirPlanChaises();
                afficherCartesEvenements();
            } else {
                maxReservationDate = null;
                txtEvenementSelectionne.clear();
                mettreAJourApercuEvenement(null);
                selectedSeats.clear();
                rafraichirPlanChaises();
                afficherCartesEvenements();
            }
        });
    }

    private void afficherCartesEvenements() {
        eventGridView.getChildren().clear();
        Evenement selectedEvent = recupererEvenementSelectionne();

        for (Evenement evenement : eventChoiceList) {
            VBox card = creerCarteEvenement(evenement, selectedEvent);
            eventGridView.getChildren().add(card);
        }

        afficherEvenementsDisponibles();
    }

    private void afficherEvenementsDisponibles() {
        if (availableEventGrid == null) {
            return;
        }

        availableEventGrid.getChildren().clear();
        for (Evenement evenement : eventChoiceList) {
            VBox card = creerCarteEvenementDisponible(evenement);
            availableEventGrid.getChildren().add(card);
        }
    }

    private VBox creerCarteEvenementDisponible(Evenement evenement) {
        VBox card = new VBox(7);
        card.setPrefSize(245, 220);
        card.getStyleClass().add("available-event-card");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(82);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("available-event-image");
        if (evenement.getImage() != null && !evenement.getImage().trim().isEmpty()) {
            try {
                Image image = new Image(resoudreSourceImage(evenement.getImage()), 220, 82, true, true, true);
                imageView.setImage(image.isError() ? null : image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        Label title = new Label(evenement.getNom());
        title.setWrapText(true);
        title.setMaxWidth(220);
        title.getStyleClass().add("available-event-title");

        Label date = new Label("Date: " + evenement.getDate_evenement());
        date.getStyleClass().add("available-event-meta");

        Label place = new Label("Lieu: " + evenement.getLieu());
        place.setWrapText(true);
        place.setMaxWidth(220);
        place.getStyleClass().add("available-event-meta");

        Label duration = new Label("Duree: " + formaterDuree(evenement.getDuree_minutes()));
        duration.getStyleClass().add("available-event-meta");

        Button reserveButton = new Button("Reserver");
        reserveButton.setPrefWidth(104);
        reserveButton.getStyleClass().add("btn-reserve");
        reserveButton.setOnAction(event -> {
            event.consume();
            demarrerReservationPourEvenement(evenement);
        });

        Button weatherButton = new Button("Meteo");
        weatherButton.setPrefWidth(104);
        weatherButton.getStyleClass().add("btn-location");
        weatherButton.setOnAction(event -> {
            event.consume();
            afficherMeteoEvenement(evenement);
        });

        HBox actionRow = new HBox(8, weatherButton, reserveButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPrefWidth(220);

        card.getChildren().addAll(imageView, title, date, place, duration, actionRow);
        card.setOnMouseClicked(event -> demarrerReservationPourEvenement(evenement));

        return card;
    }

    private void afficherMeteoEvenement(Evenement evenement) {
        Dialog<ButtonType> loadingDialog = creerDialogueSimple(
                "Meteo de l'evenement",
                "Chargement de la meteo...",
                "Connexion a Open-Meteo pour recuperer les previsions.");
        loadingDialog.show();

        CompletableFuture
                .supplyAsync(() -> chargerMeteo(evenement))
                .orTimeout(10, TimeUnit.SECONDS)
                .thenAccept(forecast -> Platform.runLater(() -> {
                    loadingDialog.close();
                    afficherDialogueMeteo(evenement, forecast);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        afficherAlerte("Meteo indisponible",
                                "La meteo n'a pas ete trouvee. Utilisez un lieu reconnu comme Tunis, Ariana, Sousse ou Sfax, puis verifiez votre connexion Internet.",
                                Alert.AlertType.WARNING);
                    });
                    return null;
                });
    }

    private WeatherService.WeatherForecast chargerMeteo(Evenement evenement) {
        try {
            return weatherService.getForecast(evenement.getLieu(), evenement.getDate_evenement().toLocalDate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void afficherDialogueMeteo(Evenement evenement, WeatherService.WeatherForecast forecast) {
        Label title = new Label("Meteo de l'evenement");
        title.getStyleClass().add("ticket-title");

        Label eventName = new Label(evenement.getNom());
        eventName.setWrapText(true);
        eventName.setMaxWidth(300);
        eventName.getStyleClass().add("ticket-event-name");

        Label location = new Label("Lieu detecte : " + forecast.getLocation());
        location.setWrapText(true);
        location.setMaxWidth(300);
        location.getStyleClass().add("ticket-meta");

        Label date = new Label("Date : " + forecast.getDate());
        date.getStyleClass().add("ticket-meta");

        Label temperature = new Label(String.format(java.util.Locale.FRANCE,
                "Temperature : %.1f C / %.1f C",
                forecast.getMinTemperature(),
                forecast.getMaxTemperature()));
        temperature.getStyleClass().add("ticket-meta");

        Label rain = new Label("Risque de pluie : " + forecast.getRainProbability() + "%");
        rain.getStyleClass().add("ticket-meta");

        Label condition = new Label("Condition : " + forecast.getDescription());
        condition.getStyleClass().add("reservation-grid-status");
        condition.getStyleClass().add("status-pending");

        VBox content = new VBox(10, title, eventName, location, date, temperature, rain, condition);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("ticket-dialog-content");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Meteo de l'evenement");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/skillora/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("ticket-dialog");

        Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Fermer");
        closeButton.getStyleClass().add("btn-primary");

        dialog.showAndWait();
    }

    private Dialog<ButtonType> creerDialogueSimple(String titleText, String heading, String message) {
        Label title = new Label(heading);
        title.getStyleClass().add("ticket-title");

        Label detail = new Label(message);
        detail.setWrapText(true);
        detail.setMaxWidth(300);
        detail.getStyleClass().add("ticket-meta");

        VBox content = new VBox(10, title, detail);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("ticket-dialog-content");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titleText);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/skillora/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("ticket-dialog");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Annuler");
        cancelButton.getStyleClass().add("btn-secondary");
        return dialog;
    }

    private VBox creerCarteEvenement(Evenement evenement, Evenement selectedEvent) {
        VBox card = new VBox(8);
        card.setPrefSize(300, 245);
        card.getStyleClass().add("event-grid-card");

        if (selectedEvent != null && selectedEvent.getId_evenement() == evenement.getId_evenement()) {
            card.getStyleClass().add("event-grid-card-selected");
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(276);
        imageView.setFitHeight(92);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("event-grid-image");
        if (evenement.getImage() != null && !evenement.getImage().trim().isEmpty()) {
            try {
                Image image = new Image(resoudreSourceImage(evenement.getImage()), 276, 92, true, true, true);
                imageView.setImage(image.isError() ? null : image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        Button reserveButton = new Button("Reserver");
        reserveButton.setPrefWidth(276);
        reserveButton.getStyleClass().add("btn-primary");
        reserveButton.setOnAction(event -> {
            event.consume();
            demarrerReservationPourEvenement(evenement);
        });

        Label title = new Label(evenement.getNom());
        title.setWrapText(true);
        title.setMaxWidth(276);
        title.getStyleClass().add("event-grid-title");

        Label date = new Label("Date : " + evenement.getDate_evenement());
        date.getStyleClass().add("event-grid-meta");

        Label place = new Label("Lieu : " + evenement.getLieu());
        place.setWrapText(true);
        place.setMaxWidth(276);
        place.getStyleClass().add("event-grid-meta");

        Label duration = new Label("Duree : " + formaterDuree(evenement.getDuree_minutes()));
        duration.getStyleClass().add("event-grid-meta");

        card.getChildren().addAll(reserveButton, imageView, title, date, place, duration);
        card.setOnMouseClicked(event -> selectionnerEvenementParId(evenement.getId_evenement()));

        return card;
    }

    private void demarrerReservationPourEvenement(Evenement evenement) {
        if (!peutCreerReservation()) {
            afficherAlerte("Acces refuse", "Seul l'etudiant peut ajouter une reservation.", Alert.AlertType.WARNING);
            return;
        }

        listReservations.getSelectionModel().clearSelection();
        selectionnerEvenementParId(evenement.getId_evenement());
        appliquerPermissionsSelonRole(null);
        afficherFormulaireReservation();
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

    private void preparerPlanChaises() {
        rafraichirPlanChaises();
    }

    private void rafraichirPlanChaises() {
        seatGrid.getChildren().clear();
        occupiedSeats = chargerChaisesOccupeesPourEvenementSelectionne();

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

                seatButton.setOnAction(event -> selectionnerOuDeselectionnerChaise(seatCode));
                seatGrid.add(seatButton, col, row);
            }
        }

        mettreAJourTexteChaisesSelectionnees();
    }

    private Set<String> chargerChaisesOccupeesPourEvenementSelectionne() {
        Set<String> seats = new LinkedHashSet<>();
        Evenement selectedEvent = recupererEvenementSelectionne();
        if (selectedEvent == null) {
            return seats;
        }

        Reservation selectedReservation = recupererReservationSelectionnee();
        int excludedReservationId = selectedReservation != null ? selectedReservation.getId_reservation() : 0;

        try {
            seats.addAll(reservationCRUD.recupererChaisesReservees(selectedEvent.getId_evenement(), excludedReservationId));
        } catch (SQLException e) {
            afficherAlerte("Erreur chaises", "Impossible de charger les chaises deja reservees.", Alert.AlertType.ERROR);
        }

        return seats;
    }

    private void selectionnerOuDeselectionnerChaise(String seatCode) {
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
        mettreAJourNombrePlaces(selectedSeats.size());
        lblSelectedSeats.getStyleClass().remove("field-error");
        rafraichirPlanChaises();
    }

    private void chargerChoixEvenementsDepuisBase() {
        try {
            allAvailableEventList.setAll(filtrerEvenementsDisponibles(evenementCRUD.afficher()));
            appliquerFiltresEvenementsDisponibles();
            listChoixEvenements.setItems(eventChoiceList);
        } catch (Exception e) {
            afficherAlerte("Erreur de connexion", "Impossible de charger la liste des evenements.", Alert.AlertType.ERROR);
        }
    }

    private void appliquerFiltresEvenementsDisponibles() {
        String search = txtRechercheReservation.getText() != null
                ? txtRechercheReservation.getText().trim().toLowerCase()
                : "";

        if (search.isEmpty()) {
            eventChoiceList.setAll(allAvailableEventList);
        } else {
            List<Evenement> filteredEvents = new ArrayList<>();
            for (Evenement evenement : allAvailableEventList) {
                String content = (evenement.getNom() + " "
                        + evenement.getLieu() + " "
                        + evenement.getDate_evenement() + " "
                        + formaterDuree(evenement.getDuree_minutes())).toLowerCase();
                if (content.contains(search)) {
                    filteredEvents.add(evenement);
                }
            }
            eventChoiceList.setAll(filteredEvents);
        }

        afficherCartesEvenements();
    }

    private List<Evenement> filtrerEvenementsDisponibles(List<Evenement> evenements) {
        List<Evenement> availableEvents = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Evenement evenement : evenements) {
            if (!evenement.getDate_evenement().toLocalDate().isBefore(today)) {
                availableEvents.add(evenement);
            }
        }

        return availableEvents;
    }

    private boolean peutCreerReservation() {
        Role role = SessionManager.getCurrentUserRole();
        return utiliseInterfaceEtudiant(role);
    }

    private boolean peutModifierReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        return role == Role.ADMIN
                || (utiliseInterfaceEtudiant(role)
                && reservation.getId_utilisateur() == currentUserId
                && !"ACCEPTEE".equals(reservation.getStatut()));
    }

    private boolean peutSupprimerReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        return role == Role.ADMIN
                || (utiliseInterfaceEtudiant(role) && reservation.getId_utilisateur() == currentUserId);
    }

    private boolean utiliseInterfaceEtudiant(Role role) {
        return role == Role.ETUDIANT || role == Role.INSTRUCTEUR;
    }

    private ListCell<Reservation> creerLigneListeReservation() {
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
                        Image image = new Image(resoudreSourceImage(reservation.getImage_evenement()), 82, 54, true, true, true);
                        imageView.setImage(image.isError() ? null : image);
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }

                VBox info = new VBox(4);
                info.setPrefWidth(460);

                Label title = new Label(formaterChaises(reservation.getNom_evenement()));
                title.setWrapText(true);
                title.getStyleClass().add("reservation-list-title");

                Label details = new Label("Etudiant: " + formaterNomUtilisateur(reservation)
                        + " | Date: " + reservation.getDate_reservation()
                        + " | Places: " + reservation.getNb_places()
                        + " | Chaises: " + formaterChaises(reservation.getChaises()));
                details.setWrapText(true);
                details.getStyleClass().add("reservation-list-meta");

                info.getChildren().addAll(title, details);

                Label status = new Label(formaterStatut(reservation.getStatut()));
                status.setMinWidth(92);
                status.setAlignment(Pos.CENTER);
                status.getStyleClass().add("reservation-list-status");
                status.getStyleClass().add(classeCssSelonStatut(reservation.getStatut()));

                row.getChildren().addAll(imageView, info, status);
                setText(null);
                setGraphic(row);
            }
        };
    }

    private ListCell<Evenement> creerLigneChoixEvenement() {
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

    private String formaterStatut(String statut) {
        if ("ACCEPTEE".equals(statut)) {
            return "Acceptee";
        }
        if ("REFUSEE".equals(statut)) {
            return "Refusee";
        }
        return "En attente";
    }

    private String classeCssSelonStatut(String statut) {
        if ("ACCEPTEE".equals(statut)) {
            return "status-accepted";
        }
        if ("REFUSEE".equals(statut)) {
            return "status-refused";
        }
        return "status-pending";
    }

    private String formaterChaises(String seats) {
        if (seats == null || seats.trim().isEmpty()) {
            return "-";
        }
        return seats;
    }

    private String formaterNomUtilisateur(Reservation reservation) {
        if (reservation.getNom_utilisateur() != null && !reservation.getNom_utilisateur().trim().isEmpty()) {
            return reservation.getNom_utilisateur();
        }
        return "Utilisateur #" + reservation.getId_utilisateur();
    }

    private Reservation recupererReservationSelectionnee() {
        return listReservations.getSelectionModel().getSelectedItem();
    }

    private void remplirFormulaire(Reservation r) {
        populatingReservation = true;
        try {
            mettreAJourNombrePlaces(r.getNb_places());
            datePicker.setValue(r.getDate_reservation().toLocalDate());
            txtEvenementSelectionne.setText(r.getNom_evenement());
            selectedSeats.clear();
            selectedSeats.addAll(convertirChaisesEnsemble(r.getChaises()));
            selectionnerEvenementParId(r.getId_evenement());
            rafraichirPlanChaises();
            retirerErreursVisuelles();
        } finally {
            populatingReservation = false;
        }
    }

    private void selectionnerEvenementParId(int idEvenement) {
        for (Evenement evenement : eventChoiceList) {
            if (evenement.getId_evenement() == idEvenement) {
                listChoixEvenements.getSelectionModel().select(evenement);
                listChoixEvenements.scrollTo(evenement);
                return;
            }
        }
        listChoixEvenements.getSelectionModel().clearSelection();
    }

    private void chargerReservationsDepuisBase() {
        try {
            List<Reservation> data = filtrerReservationsSelonRole(reservationCRUD.afficher());
            allReservationList.setAll(data);
            appliquerFiltresReservations();
        } catch (Exception e) {
            afficherAlerte("Erreur de connexion", "Impossible de contacter la base de donnees. Assurez-vous que MySQL est lance sur le port 3306.", Alert.AlertType.ERROR);
        }
    }

    private void appliquerFiltresReservations() {
        String search = txtRechercheReservation.getText() != null
                ? txtRechercheReservation.getText().trim().toLowerCase()
                : "";
        String statusFilter = comboFiltreStatut.getValue() != null ? comboFiltreStatut.getValue() : "Tous";

        List<Reservation> filteredReservations = new ArrayList<>();
        for (Reservation reservation : allReservationList) {
            if (!correspondAuFiltreStatut(reservation, statusFilter)) {
                continue;
            }
            if (!correspondARechercheReservation(reservation, search)) {
                continue;
            }
            filteredReservations.add(reservation);
        }

        reservationList.setAll(filteredReservations);
        listReservations.setItems(reservationList);
        afficherCartesReservations();
        appliquerPermissionsSelonRole(recupererReservationSelectionnee());
    }

    private boolean correspondAuFiltreStatut(Reservation reservation, String statusFilter) {
        if ("Tous".equals(statusFilter)) {
            return true;
        }
        return formaterStatut(reservation.getStatut()).equals(statusFilter);
    }

    private boolean correspondARechercheReservation(Reservation reservation, String search) {
        if (search.isEmpty()) {
            return true;
        }

        String content = (formaterChaises(reservation.getNom_evenement()) + " "
                + formaterNomUtilisateur(reservation) + " "
                + formaterChaises(reservation.getChaises()) + " "
                + reservation.getDate_reservation() + " "
                + formaterStatut(reservation.getStatut())).toLowerCase();
        return content.contains(search);
    }

    private void afficherCartesReservations() {
        reservationGridView.getChildren().clear();

        for (Reservation reservation : reservationList) {
            VBox card = creerCarteReservation(reservation);
            reservationGridView.getChildren().add(card);
        }
    }

    private VBox creerCarteReservation(Reservation reservation) {
        VBox card = new VBox(8);
        card.setPrefSize(245, 230);
        card.getStyleClass().add("reservation-grid-card");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(72);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("reservation-grid-image");
        if (reservation.getImage_evenement() != null && !reservation.getImage_evenement().trim().isEmpty()) {
            try {
                Image image = new Image(resoudreSourceImage(reservation.getImage_evenement()), 220, 72, true, true, true);
                imageView.setImage(image.isError() ? null : image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        Label title = new Label(formaterChaises(reservation.getNom_evenement()));
        title.setWrapText(true);
        title.setMaxWidth(220);
        title.getStyleClass().add("reservation-grid-title");

        Label date = new Label("Date: " + reservation.getDate_reservation());
        date.getStyleClass().add("reservation-grid-meta");

        Label seats = new Label("Chaises: " + formaterChaises(reservation.getChaises()));
        seats.setWrapText(true);
        seats.setMaxWidth(220);
        seats.getStyleClass().add("reservation-grid-meta");

        Label status = new Label(formaterStatut(reservation.getStatut()));
        status.getStyleClass().add("reservation-grid-status");
        status.getStyleClass().add(classeCssSelonStatut(reservation.getStatut()));

        Button ticketButton = new Button("Voir le billet");
        ticketButton.setPrefWidth(200);
        ticketButton.getStyleClass().add("btn-reserve");
        ticketButton.setOnAction(event -> {
            event.consume();
            afficherQrReservation(reservation);
        });

        VBox bottomRow = new VBox(6, title, date, seats, status, ticketButton);

        card.getChildren().addAll(imageView, bottomRow);
        card.setOnMouseClicked(event -> {
            listReservations.getSelectionModel().select(reservation);
            appliquerPermissionsSelonRole(reservation);
        });

        return card;
    }

    private void exporterReservationPdf(Reservation reservation) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la reservation en PDF");
        fileChooser.setInitialFileName("reservation-" + reservation.getId_reservation() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File selectedFile = fileChooser.showSaveDialog(reservationListPage.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            genererPdfReservation(reservation, selectedFile);
            afficherAlerte("Export reussi", "La reservation a ete exportee en PDF.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            afficherAlerte("Erreur export PDF", "Impossible d'exporter cette reservation en PDF.", Alert.AlertType.ERROR);
        }
    }

    private void genererPdfReservation(Reservation reservation, File file) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 48, 48, 44, 44);
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(
                com.lowagie.text.FontFactory.HELVETICA_BOLD, 20, new java.awt.Color(11, 47, 95));
        com.lowagie.text.Font sectionFont = com.lowagie.text.FontFactory.getFont(
                com.lowagie.text.FontFactory.HELVETICA_BOLD, 13, new java.awt.Color(15, 95, 196));
        com.lowagie.text.Font textFont = com.lowagie.text.FontFactory.getFont(
                com.lowagie.text.FontFactory.HELVETICA, 12, new java.awt.Color(49, 95, 155));

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("Skillora - Billet de reservation", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(22);
        document.add(title);

        document.add(new com.lowagie.text.Paragraph("Details de la reservation", sectionFont));
        ajouterLignePdf(document, "Reservation", "#" + reservation.getId_reservation(), textFont);
        ajouterLignePdf(document, "Evenement", formaterChaises(reservation.getNom_evenement()), textFont);
        ajouterLignePdf(document, "Lieu", formaterChaises(reservation.getLieu_evenement()), textFont);
        ajouterLignePdf(document, "Date", String.valueOf(reservation.getDate_reservation()), textFont);
        ajouterLignePdf(document, "Chaises", formaterChaises(reservation.getChaises()), textFont);
        ajouterLignePdf(document, "Statut", formaterStatut(reservation.getStatut()), textFont);

        com.lowagie.text.Paragraph qrTitle = new com.lowagie.text.Paragraph("Billet QR", sectionFont);
        qrTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        qrTitle.setSpacingBefore(26);
        qrTitle.setSpacingAfter(10);
        document.add(qrTitle);

        com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(genererUrlQrReservation(reservation));
        qrImage.scaleAbsolute(160, 160);
        qrImage.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        document.add(qrImage);

        com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph(
                "Presentez ce billet lors de l'acces a l'evenement.", textFont);
        footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        footer.setSpacingBefore(18);
        document.add(footer);

        document.close();
    }

    private void ajouterLignePdf(com.lowagie.text.Document document, String label, String value, com.lowagie.text.Font font)
            throws com.lowagie.text.DocumentException {
        document.add(new com.lowagie.text.Paragraph(label + " : " + value, font));
    }

    private void afficherQrReservation(Reservation reservation) {
        ImageView qrCode = new ImageView();
        qrCode.setFitWidth(170);
        qrCode.setFitHeight(170);
        qrCode.setPreserveRatio(true);
        qrCode.getStyleClass().add("ticket-qr-image");
        qrCode.setImage(new Image(genererUrlQrReservation(reservation), 170, 170, true, true, true));

        Label title = new Label("Billet de reservation");
        title.getStyleClass().add("ticket-title");

        Label eventName = new Label(formaterChaises(reservation.getNom_evenement()));
        eventName.setWrapText(true);
        eventName.setMaxWidth(280);
        eventName.getStyleClass().add("ticket-event-name");

        Label reservationNumber = new Label("Reservation #" + reservation.getId_reservation());
        reservationNumber.getStyleClass().add("ticket-meta");

        Label date = new Label("Date : " + reservation.getDate_reservation());
        date.getStyleClass().add("ticket-meta");

        Label seats = new Label("Chaises : " + formaterChaises(reservation.getChaises()));
        seats.setWrapText(true);
        seats.setMaxWidth(280);
        seats.getStyleClass().add("ticket-meta");

        Label status = new Label(formaterStatut(reservation.getStatut()));
        status.getStyleClass().add("reservation-grid-status");
        status.getStyleClass().add(classeCssSelonStatut(reservation.getStatut()));

        Button locationButton = new Button("Itineraire");
        locationButton.setPrefWidth(120);
        locationButton.getStyleClass().add("btn-location");
        locationButton.setOnAction(event -> ouvrirLocalisationReservation(reservation));

        Button exportButton = new Button("Exporter PDF");
        exportButton.setPrefWidth(130);
        exportButton.getStyleClass().add("btn-secondary");
        exportButton.setOnAction(event -> exporterReservationPdf(reservation));

        HBox ticketActions = new HBox(10, locationButton, exportButton);
        ticketActions.setAlignment(Pos.CENTER);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("ticket-dialog-content");
        content.getChildren().addAll(title, eventName, reservationNumber, date, seats, status, qrCode, ticketActions);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Billet de reservation");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/skillora/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("ticket-dialog");

        Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Fermer");
        closeButton.getStyleClass().add("btn-primary");

        dialog.showAndWait();
    }

    private String genererUrlQrReservation(Reservation reservation) {
        String data = "Skillora Reservation"
                + "\nReservation: " + reservation.getId_reservation()
                + "\nEvenement: " + formaterChaises(reservation.getNom_evenement())
                + "\nLieu: " + formaterChaises(reservation.getLieu_evenement())
                + "\nDate: " + reservation.getDate_reservation()
                + "\nChaises: " + formaterChaises(reservation.getChaises())
                + "\nStatut: " + formaterStatut(reservation.getStatut());
        String encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=" + encodedData;
    }

    private void ouvrirLocalisationReservation(Reservation reservation) {
        String query = reservation.getLieu_evenement() != null && !reservation.getLieu_evenement().trim().isEmpty()
                ? reservation.getLieu_evenement() + " " + reservation.getNom_evenement()
                : reservation.getNom_evenement();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        ouvrirLienExterne("https://www.google.com/maps/search/?api=1&query=" + encodedQuery,
                "Impossible d'ouvrir la localisation de cette reservation.");
    }

    private void ouvrirLienExterne(String url, String errorMessage) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                afficherAlerte("Navigation indisponible", "Votre systeme ne permet pas d'ouvrir le navigateur automatiquement.", Alert.AlertType.WARNING);
                return;
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            afficherAlerte("Erreur", errorMessage, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void ouvrirFormulaireAjout(ActionEvent event) {
        listReservations.getSelectionModel().clearSelection();
        viderChampsFormulaire();
        appliquerPermissionsSelonRole(null);
        afficherFormulaireReservation();
    }

    @FXML
    private void basculerMesReservations(ActionEvent event) {
        afficherMesReservations = !afficherMesReservations;
        listReservations.getSelectionModel().clearSelection();
        txtRechercheReservation.clear();
        comboFiltreStatut.setValue("Tous");
        appliquerFiltresEcranPrincipal();
        appliquerPermissionsSelonRole(null);
    }

    @FXML
    private void ouvrirFormulaireModification(ActionEvent event) {
        Reservation selected = recupererReservationSelectionnee();
        if (selected == null) {
            afficherAlerte("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        remplirFormulaire(selected);
        appliquerPermissionsSelonRole(selected);
        afficherFormulaireReservation();
    }

    @FXML
    private void retournerALaListeReservations(ActionEvent event) {
        afficherListeReservations();
    }

    private void afficherListeReservations() {
        rendreElementVisible(reservationListPage, true);
        rendreElementVisible(reservationFormPage, false);
    }

    private void afficherFormulaireReservation() {
        rendreElementVisible(reservationListPage, false);
        rendreElementVisible(reservationFormPage, true);
    }

    private List<Reservation> filtrerReservationsSelonRole(List<Reservation> reservations) {
        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();

        if (role == null) {
            return new ArrayList<>();
        }

        if (role == Role.ADMIN) {
            return reservations;
        }

        List<Reservation> filteredReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (utiliseInterfaceEtudiant(role) && reservation.getId_utilisateur() == currentUserId) {
                filteredReservations.add(reservation);
            }
        }
        return filteredReservations;
    }

    @FXML
    private void ajouterDepuisFormulaire(ActionEvent event) {
        if (!peutCreerReservation()) {
            afficherAlerte("Acces refuse", "Seul l'etudiant peut ajouter une reservation.", Alert.AlertType.WARNING);
            return;
        }

        if (validerSaisieFormulaire()) {
            Reservation r = creerReservationDepuisFormulaire();
            try {
                reservationCRUD.ajouter(r);
                chargerReservationsDepuisBase();
                viderFormulaire();
                afficherAlerte("Reservation envoyee", "Reservation envoyee, en attente de validation admin.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void modifierDepuisFormulaire(ActionEvent event) {
        Reservation selected = recupererReservationSelectionnee();
        if (!peutModifierReservation(selected)) {
            if (selected != null
                    && SessionManager.getCurrentUserRole() == Role.ETUDIANT
                    && "ACCEPTEE".equals(selected.getStatut())) {
                afficherAlerte("Modification impossible", "Cette reservation est deja acceptee. Vous ne pouvez plus la modifier.", Alert.AlertType.WARNING);
            } else {
                afficherAlerte("Acces refuse", "Vous ne pouvez modifier que vos propres reservations.", Alert.AlertType.WARNING);
            }
            return;
        }

        if (validerSaisieFormulaire()) {
            if (reservationSelectionneeSansChangement(selected)) {
                afficherAlerte("Aucune modification", "Aucune modification detectee.", Alert.AlertType.INFORMATION);
                return;
            }

            Reservation r = creerReservationDepuisFormulaire(selected.getId_utilisateur());
            r.setId_reservation(selected.getId_reservation());
            try {
                reservationCRUD.modifier(r);
                chargerReservationsDepuisBase();
                viderFormulaire();
                afficherAlerte("Modification reussie", "La reservation a ete modifiee avec succes.", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean reservationSelectionneeSansChangement(Reservation selected) {
        if (selected == null || datePicker.getValue() == null || recupererEvenementSelectionne() == null) {
            return false;
        }

        String selectedChaises = selected.getChaises() != null ? selected.getChaises().trim() : "";
        String formChaises = String.join(",", selectedSeats);

        return selected.getNb_places() == selectedSeats.size()
                && selected.getDate_reservation().toLocalDate().equals(datePicker.getValue())
                && selected.getId_evenement() == recupererEvenementSelectionne().getId_evenement()
                && selectedChaises.equals(formChaises);
    }

    @FXML
    private void annulerModificationFormulaire(ActionEvent event) {
        viderFormulaire();
    }

    @FXML
    private void supprimerElementSelectionne(ActionEvent event) {
        Reservation selected = recupererReservationSelectionnee();
        if (selected == null) {
            afficherAlerte("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }
        if (!peutSupprimerReservation(selected)) {
            afficherAlerte("Acces refuse", "Vous ne pouvez supprimer que les reservations autorisees pour votre role.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Cette reservation sera supprimee definitivement. Confirmer la suppression ?");
        appliquerStyleAlerte(confirm, "alert-confirmation");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            reservationCRUD.supprimer(selected.getId_reservation());
            chargerReservationsDepuisBase();
            viderFormulaire();
            afficherAlerte("Suppression reussie", "La reservation a ete supprimee.", Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void accepterReservationSelectionnee(ActionEvent event) {
        changerStatutReservation(
                "ACCEPTEE",
                "La reservation a ete acceptee avec succes. L'etudiant peut maintenant participer a l'evenement.",
                "Confirmation d'acceptation",
                "Confirmer l'acceptation de cette reservation ?"
        );
    }

    @FXML
    private void refuserReservationSelectionnee(ActionEvent event) {
        changerStatutReservation(
                "REFUSEE",
                "La reservation a ete refusee. Les places choisies sont de nouveau disponibles.",
                "Confirmation de refus",
                "Confirmer le refus de cette reservation ?"
        );
    }

    private void changerStatutReservation(String statut, String successMessage, String confirmTitle, String confirmMessage) {
        Reservation selected = recupererReservationSelectionnee();
        if (selected == null) {
            afficherAlerte("Selection requise", "Veuillez selectionner une reservation dans la liste.", Alert.AlertType.WARNING);
            return;
        }

        if (!peutDeciderReservation(selected)) {
            afficherAlerte("Acces refuse", "Vous ne pouvez accepter ou refuser que les reservations autorisees pour votre role.", Alert.AlertType.WARNING);
            return;
        }

        if (!confirmerAction(confirmTitle, confirmMessage)) {
            return;
        }

        try {
            reservationCRUD.changerStatut(selected.getId_reservation(), statut);
            chargerReservationsDepuisBase();
            viderFormulaire();
            afficherAlerte("Decision enregistree", successMessage, Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            afficherAlerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean confirmerAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        appliquerStyleAlerte(alert, "alert-confirmation");
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    private boolean peutDeciderReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        Role role = SessionManager.getCurrentUserRole();
        long currentUserId = SessionManager.getCurrentUserId();
        return role == Role.ADMIN;
    }

    @FXML
    private void diminuerNombrePlaces(ActionEvent event) {
        mettreAJourNombrePlaces(nbPlaces - 1);
    }

    @FXML
    private void augmenterNombrePlaces(ActionEvent event) {
        mettreAJourNombrePlaces(nbPlaces + 1);
    }

    private void mettreAJourNombrePlaces(int value) {
        nbPlaces = Math.max(0, value);
        lblNbPlaces.setText(String.valueOf(nbPlaces));
        lblNbPlaces.getStyleClass().remove("field-error");
    }

    private Reservation creerReservationDepuisFormulaire() {
        return creerReservationDepuisFormulaire(SessionManager.getCurrentUserId());
    }

    private Reservation creerReservationDepuisFormulaire(long userId) {
        Reservation r = new Reservation();
        r.setNb_places(selectedSeats.size());
        r.setDate_reservation(Date.valueOf(datePicker.getValue()));
        r.setId_evenement(recupererEvenementSelectionne().getId_evenement());
        r.setId_utilisateur(userId);
        r.setChaises(String.join(",", selectedSeats));
        return r;
    }

    private void viderFormulaire() {
        viderChampsFormulaire();
        listReservations.getSelectionModel().clearSelection();
        appliquerPermissionsSelonRole(null);
        afficherListeReservations();
    }

    private void viderChampsFormulaire() {
        mettreAJourNombrePlaces(1);
        datePicker.setValue(null);
        txtEvenementSelectionne.clear();
        maxReservationDate = null;
        selectedSeats.clear();
        mettreAJourApercuEvenement(null);
        listChoixEvenements.getSelectionModel().clearSelection();
        rafraichirPlanChaises();
        retirerErreursVisuelles();
    }

    private void mettreAJourApercuEvenement(Evenement evenement) {
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
            Image image = new Image(resoudreSourceImage(imagePath), 300, 180, true, true, true);
            eventPreviewImage.setImage(image.isError() ? null : image);
        } catch (Exception e) {
            eventPreviewImage.setImage(null);
        }
    }

    private boolean validerSaisieFormulaire() {
        retirerErreursVisuelles();
        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        if (selectedSeats.isEmpty()) {
            marquerChampInvalide(lblSelectedSeats);
            errors.append("- Vous devez choisir au moins une chaise.\n");
            valid = false;
        }
        LocalDate reservationDate = datePicker.getValue();
        Evenement selectedEvent = recupererEvenementSelectionne();

        if (reservationDate == null) {
            marquerChampInvalide(datePicker);
            errors.append("- La date de reservation est obligatoire.\n");
            valid = false;
        } else if (reservationDate.isBefore(LocalDate.now())) {
            marquerChampInvalide(datePicker);
            errors.append("- La date de reservation ne doit pas etre avant aujourd'hui.\n");
            valid = false;
        }
        if (selectedEvent == null) {
            marquerChampInvalide(txtEvenementSelectionne);
            errors.append("- Vous devez choisir un evenement dans la liste.\n");
            valid = false;
        } else {
            LocalDate eventDate = selectedEvent.getDate_evenement().toLocalDate();
            if (eventDate.isBefore(LocalDate.now())) {
                marquerChampInvalide(txtEvenementSelectionne);
                errors.append("- Cet evenement est deja passe, il ne peut plus etre reserve.\n");
                valid = false;
            } else if (reservationDate != null && reservationDate.isAfter(eventDate)) {
                marquerChampInvalide(datePicker);
                errors.append("- La date de reservation ne doit pas depasser la date de l'evenement.\n");
                valid = false;
            }
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

    private Set<String> convertirChaisesEnsemble(String seats) {
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

    private void mettreAJourTexteChaisesSelectionnees() {
        if (selectedSeats.isEmpty()) {
            lblSelectedSeats.setText("Aucune chaise choisie");
            mettreAJourNombrePlaces(0);
            return;
        }

        lblSelectedSeats.setText("Chaises: " + String.join(", ", selectedSeats));
        mettreAJourNombrePlaces(selectedSeats.size());
    }

    private Evenement recupererEvenementSelectionne() {
        Evenement selected = listChoixEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        return null;
    }

    private void marquerChampInvalide(Control c) {
        c.getStyleClass().add("field-error");
    }

    private void retirerErreursVisuelles() {
        lblNbPlaces.getStyleClass().remove("field-error");
        lblSelectedSeats.getStyleClass().remove("field-error");
        datePicker.getStyleClass().remove("field-error");
        txtEvenementSelectionne.getStyleClass().remove("field-error");
    }

    private void afficherAlerte(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        appliquerStyleAlerte(alert, classeStyleSelonTypeAlerte(type));
        alert.showAndWait();
    }

    private void appliquerStyleAlerte(Alert alert, String styleClass) {
        String stylesheet = getClass().getResource("/com/skillora/style.css").toExternalForm();
        alert.getDialogPane().getStylesheets().add(stylesheet);
        alert.getDialogPane().getStyleClass().add("alert-dialog");
        alert.getDialogPane().getStyleClass().add(styleClass);
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
