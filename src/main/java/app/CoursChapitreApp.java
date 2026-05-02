package app;

import entities.Chapitre;
import entities.Cours;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Interfaces.IChapitreService;
import Interfaces.ICoursService;
import services.ChapitreService;
import services.CoursService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

public class CoursChapitreApp extends Application {

    private final ICoursService coursService = new CoursService();
    private final IChapitreService chapitreService = new ChapitreService();

    private final ObservableList<Cours> coursData = FXCollections.observableArrayList();
    private final ObservableList<Chapitre> chapitreData = FXCollections.observableArrayList();

    private TableView<Cours> coursTable;
    private TableView<Chapitre> chapitreTable;

    private TextField coursTitreField;
    private TextArea coursDescriptionArea;
    private TextField coursDomaineField;
    private ComboBox<String> coursNiveauCombo;
    private TextField coursDureeField;
    private DatePicker coursDatePicker;
    private TextField coursInstructeurField;
    private TextField coursProgressionField;

    private TextField chapitreTitreField;
    private TextArea chapitreContenuArea;
    private TextField chapitreOrdreField;
    private TextField chapitreDureeField;
    private TextField chapitrePdfField;
    private ComboBox<Cours> chapitreCoursCombo;
    private TextArea chapitreResumeArea;
    private ComboBox<String> chapitreTypeCombo;
    private TextArea chapitreExplicationArea;
    private TextArea chapitreQuizArea;

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Cours", createCoursView()));
        tabs.getTabs().add(new Tab("Chapitres", createChapitreView()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));

        Scene scene = new Scene(tabs, 1120, 720);
        stage.setTitle("Skillora - Gestion Cours et Chapitres");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.show();

        loadCours();
        loadChapitres();
    }

    private BorderPane createCoursView() {
        coursTable = new TableView<>();
        coursTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        coursTable.getColumns().addAll(Arrays.asList(
                column("ID", c -> c.getIdCours()),
                column("Titre", Cours::getTitre),
                column("Domaine", Cours::getDomaine),
                column("Niveau", Cours::getNiveau),
                column("Duree", c -> c.getDuree()),
                column("Progression", c -> c.getProgression())
        ));
        coursTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> fillCoursForm(selected));

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setCenter(coursTable);
        pane.setRight(createCoursForm());
        return pane;
    }

    private VBox createCoursForm() {
        coursTitreField = new TextField();
        coursDescriptionArea = textArea(4);
        coursDomaineField = new TextField();
        coursNiveauCombo = new ComboBox<>(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        coursDureeField = new TextField();
        coursDatePicker = new DatePicker(LocalDate.now());
        coursInstructeurField = new TextField();
        coursProgressionField = new TextField("0");

        GridPane form = grid();
        addRow(form, 0, "Titre", coursTitreField);
        addRow(form, 1, "Description", coursDescriptionArea);
        addRow(form, 2, "Domaine", coursDomaineField);
        addRow(form, 3, "Niveau", coursNiveauCombo);
        addRow(form, 4, "Duree", coursDureeField);
        addRow(form, 5, "Date", coursDatePicker);
        addRow(form, 6, "ID instructeur", coursInstructeurField);
        addRow(form, 7, "Progression", coursProgressionField);

        Button addButton = button("Ajouter", () -> saveCours(false));
        Button updateButton = button("Modifier", () -> saveCours(true));
        Button deleteButton = button("Supprimer", this::deleteCours);
        Button clearButton = button("Vider", this::clearCoursForm);

        HBox actions = actions(addButton, updateButton, deleteButton, clearButton);
        return sidePanel("Formulaire cours", form, actions);
    }

    private BorderPane createChapitreView() {
        chapitreTable = new TableView<>();
        chapitreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        chapitreTable.getColumns().addAll(Arrays.asList(
                column("ID", c -> c.getIdChapitre()),
                column("Titre", Chapitre::getTitre),
                column("Ordre", c -> c.getOrdre()),
                column("Duree", c -> c.getDuree()),
                column("Cours", c -> c.getIdCours()),
                column("Type", Chapitre::getTypeExplication)
        ));
        chapitreTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> fillChapitreForm(selected));

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setCenter(chapitreTable);
        pane.setRight(createChapitreForm());
        return pane;
    }

    private VBox createChapitreForm() {
        chapitreTitreField = new TextField();
        chapitreContenuArea = textArea(3);
        chapitreOrdreField = new TextField("1");
        chapitreDureeField = new TextField();
        chapitrePdfField = new TextField();
        chapitreCoursCombo = new ComboBox<>(coursData);
        chapitreCoursCombo.setCellFactory(list -> coursCell());
        chapitreCoursCombo.setButtonCell(coursCell());
        chapitreResumeArea = textArea(3);
        chapitreTypeCombo = new ComboBox<>(FXCollections.observableArrayList("TEXTE", "VIDEO"));
        chapitreTypeCombo.setValue("TEXTE");
        chapitreExplicationArea = textArea(3);
        chapitreQuizArea = textArea(3);

        GridPane form = grid();
        addRow(form, 0, "Titre", chapitreTitreField);
        addRow(form, 1, "Cours", chapitreCoursCombo);
        addRow(form, 2, "Contenu", chapitreContenuArea);
        addRow(form, 3, "Ordre", chapitreOrdreField);
        addRow(form, 4, "Duree", chapitreDureeField);
        addRow(form, 5, "PDF", chapitrePdfField);
        addRow(form, 6, "Resume", chapitreResumeArea);
        addRow(form, 7, "Type", chapitreTypeCombo);
        addRow(form, 8, "Explication", chapitreExplicationArea);
        addRow(form, 9, "Quiz JSON", chapitreQuizArea);

        Button addButton = button("Ajouter", () -> saveChapitre(false));
        Button updateButton = button("Modifier", () -> saveChapitre(true));
        Button deleteButton = button("Supprimer", this::deleteChapitre);
        Button clearButton = button("Vider", this::clearChapitreForm);

        HBox actions = actions(addButton, updateButton, deleteButton, clearButton);
        return sidePanel("Formulaire chapitre", form, actions);
    }

    private void saveCours(boolean update) {
        try {
            Cours cours = update ? selectedCours() : new Cours();
            if (cours == null) {
                warn("Selectionnez un cours a modifier.");
                return;
            }

            cours.setTitre(required(coursTitreField, "Titre"));
            cours.setDescription(coursDescriptionArea.getText());
            cours.setDomaine(coursDomaineField.getText());
            cours.setNiveau(valueOrDefault(coursNiveauCombo, "DEBUTANT"));
            cours.setDuree(parseInt(coursDureeField, "Duree"));
            cours.setDateCreation(coursDatePicker.getValue());
            cours.setIdInstructeur(parseLongOrNull(coursInstructeurField));
            cours.setProgression(parseInt(coursProgressionField, "Progression"));

            if (update) {
                coursService.update(cours);
            } else {
                coursService.add(cours);
            }

            loadCours();
            clearCoursForm();
        } catch (Exception e) {
            error(e);
        }
    }

    private void saveChapitre(boolean update) {
        try {
            Chapitre chapitre = update ? selectedChapitre() : new Chapitre();
            if (chapitre == null) {
                warn("Selectionnez un chapitre a modifier.");
                return;
            }

            Cours cours = chapitreCoursCombo.getValue();
            if (cours == null) {
                warn("Selectionnez le cours du chapitre.");
                return;
            }

            chapitre.setTitre(required(chapitreTitreField, "Titre"));
            chapitre.setContenu(chapitreContenuArea.getText());
            chapitre.setOrdre(parseInt(chapitreOrdreField, "Ordre"));
            chapitre.setDuree(parseInt(chapitreDureeField, "Duree"));
            chapitre.setPdfUrl(chapitrePdfField.getText());
            chapitre.setIdCours(cours.getIdCours());
            chapitre.setResume(chapitreResumeArea.getText());
            chapitre.setTypeExplication(valueOrDefault(chapitreTypeCombo, "TEXTE"));
            chapitre.setExplication(chapitreExplicationArea.getText());
            chapitre.setQuizJson(chapitreQuizArea.getText());

            if (update) {
                chapitreService.update(chapitre);
            } else {
                chapitreService.add(chapitre);
            }

            loadChapitres();
            clearChapitreForm();
        } catch (Exception e) {
            error(e);
        }
    }

    private void deleteCours() {
        Cours cours = selectedCours();
        if (cours == null) {
            warn("Selectionnez un cours a supprimer.");
            return;
        }

        try {
            coursService.delete(cours);
            loadCours();
            loadChapitres();
            clearCoursForm();
        } catch (SQLException e) {
            error(e);
        }
    }

    private void deleteChapitre() {
        Chapitre chapitre = selectedChapitre();
        if (chapitre == null) {
            warn("Selectionnez un chapitre a supprimer.");
            return;
        }

        try {
            chapitreService.delete(chapitre);
            loadChapitres();
            clearChapitreForm();
        } catch (SQLException e) {
            error(e);
        }
    }

    private void loadCours() {
        try {
            coursData.setAll(coursService.getAll());
            if (coursTable != null) {
                coursTable.setItems(coursData);
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private void loadChapitres() {
        try {
            chapitreData.setAll(chapitreService.getAll());
            if (chapitreTable != null) {
                chapitreTable.setItems(chapitreData);
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private void fillCoursForm(Cours cours) {
        if (cours == null) {
            return;
        }
        coursTitreField.setText(cours.getTitre());
        coursDescriptionArea.setText(cours.getDescription());
        coursDomaineField.setText(cours.getDomaine());
        String niveau = cours.getNiveau();
        if (niveau != null && coursNiveauCombo.getItems().contains(niveau)) {
            coursNiveauCombo.setValue(niveau);
        } else {
            coursNiveauCombo.setValue("DEBUTANT");
        }
        coursDureeField.setText(String.valueOf(cours.getDuree()));
        coursDatePicker.setValue(cours.getDateCreation());
        coursInstructeurField.setText(cours.getIdInstructeur() == null ? "" : String.valueOf(cours.getIdInstructeur()));
        coursProgressionField.setText(String.valueOf(cours.getProgression()));
    }

    private void fillChapitreForm(Chapitre chapitre) {
        if (chapitre == null) {
            return;
        }
        chapitreTitreField.setText(chapitre.getTitre());
        chapitreContenuArea.setText(chapitre.getContenu());
        chapitreOrdreField.setText(String.valueOf(chapitre.getOrdre()));
        chapitreDureeField.setText(String.valueOf(chapitre.getDuree()));
        chapitrePdfField.setText(chapitre.getPdfUrl());
        chapitreCoursCombo.setValue(findCours(chapitre.getIdCours()));
        chapitreResumeArea.setText(chapitre.getResume());
        String typeEx = chapitre.getTypeExplication();
        chapitreTypeCombo.setValue(typeEx == null || typeEx.isEmpty() ? "TEXTE" : typeEx);
        chapitreExplicationArea.setText(chapitre.getExplication());
        chapitreQuizArea.setText(chapitre.getQuizJson());
    }

    private Cours findCours(int idCours) {
        return coursData.stream()
                .filter(cours -> cours.getIdCours() == idCours)
                .findFirst()
                .orElse(null);
    }

    private void clearCoursForm() {
        coursTable.getSelectionModel().clearSelection();
        coursTitreField.clear();
        coursDescriptionArea.clear();
        coursDomaineField.clear();
        coursNiveauCombo.setValue("DEBUTANT");
        coursDureeField.clear();
        coursDatePicker.setValue(LocalDate.now());
        coursInstructeurField.clear();
        coursProgressionField.setText("0");
    }

    private void clearChapitreForm() {
        chapitreTable.getSelectionModel().clearSelection();
        chapitreTitreField.clear();
        chapitreContenuArea.clear();
        chapitreOrdreField.setText("1");
        chapitreDureeField.clear();
        chapitrePdfField.clear();
        chapitreCoursCombo.setValue(null);
        chapitreResumeArea.clear();
        chapitreTypeCombo.setValue("TEXTE");
        chapitreExplicationArea.clear();
        chapitreQuizArea.clear();
    }

    private Cours selectedCours() {
        return coursTable.getSelectionModel().getSelectedItem();
    }

    private Chapitre selectedChapitre() {
        return chapitreTable.getSelectionModel().getSelectedItem();
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    private static VBox sidePanel(String title, GridPane form, HBox actions) {
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        VBox box = new VBox(12, label, form, actions);
        box.setPadding(new Insets(0, 0, 0, 16));
        box.setPrefWidth(390);
        return box;
    }

    private static HBox actions(Button... buttons) {
        HBox hbox = new HBox(8, buttons);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        return hbox;
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static TextArea textArea(int rows) {
        TextArea area = new TextArea();
        area.setPrefRowCount(rows);
        area.setWrapText(true);
        return area;
    }

    private static void addRow(GridPane grid, int row, String labelText, Control control) {
        Label label = new Label(labelText);
        label.setMinWidth(95);
        control.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(control, Priority.ALWAYS);
        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    private static <S, T> TableColumn<S, T> column(String title, ValueProvider<S, T> provider) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleObjectProperty<>(provider.get(data.getValue())));
        return column;
    }

    private static ListCell<Cours> coursCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Cours cours, boolean empty) {
                super.updateItem(cours, empty);
                setText(empty || cours == null ? "" : cours.getIdCours() + " - " + cours.getTitre());
            }
        };
    }

    private static String required(TextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " est obligatoire.");
        }
        return value;
    }

    private static String valueOrDefault(ComboBox<String> comboBox, String defaultValue) {
        return comboBox.getValue() == null ? defaultValue : comboBox.getValue();
    }

    private static int parseInt(TextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " doit etre un nombre.");
        }
    }

    private static Long parseLongOrNull(TextField field) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID instructeur doit etre un nombre.");
        }
    }

    private static void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static void error(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private interface ValueProvider<S, T> {
        T get(S source);
    }

}
