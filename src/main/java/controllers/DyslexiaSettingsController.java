package controllers;

import entities.UserPreference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.DyslexiaService;
import services.SessionService;

import java.util.function.Consumer;

public class DyslexiaSettingsController {

    @FXML private ToggleButton btnToggleMode;
    @FXML private Slider sliderTextSize;
    @FXML private Label lblTextSize;
    @FXML private Slider sliderLetterSpacing;
    @FXML private Slider sliderWordSpacing;
    @FXML private Slider sliderLineSpacing;
    @FXML private CheckBox checkSyllables;
    @FXML private CheckBox checkColorSyllables;
    @FXML private ComboBox<String> comboFont;
    @FXML private CheckBox checkFocus;
    @FXML private Slider sliderAudioSpeed;
    @FXML private Label lblAudioSpeed;

    private DyslexiaService dyslexiaService = new DyslexiaService();
    private UserPreference currentPref;
    private Consumer<UserPreference> onUpdate;

    @FXML
    public void initialize() {
        comboFont.getItems().addAll("OpenDyslexic", "Arial", "Verdana", "Tahoma", "Comic Sans MS", "Lexie Readable");
        
        // Listeners for real-time updates
        sliderTextSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            int size = newVal.intValue();
            lblTextSize.setText(size + "px");
            if (currentPref != null) {
                currentPref.setTaillePolice(size);
                notifyUpdate();
            }
        });

        sliderAudioSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = Math.round(newVal.doubleValue() * 10) / 10.0;
            lblAudioSpeed.setText(speed + "x");
            if (currentPref != null) {
                currentPref.setVitesseAudio(speed);
                notifyUpdate();
            }
        });

        sliderLetterSpacing.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPref != null) {
                currentPref.setEspacementLettres(newVal.doubleValue());
                notifyUpdate();
            }
        });

        sliderWordSpacing.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPref != null) {
                currentPref.setEspacementMots(newVal.doubleValue());
                notifyUpdate();
            }
        });

        sliderLineSpacing.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPref != null) {
                currentPref.setInterligne(newVal.doubleValue());
                notifyUpdate();
            }
        });
    }

    public void setOnUpdate(Consumer<UserPreference> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void loadPreferences(long userId) {
        this.currentPref = dyslexiaService.getPreferences(userId);
        updateUI();
    }

    private void updateUI() {
        if (currentPref == null) return;
        
        sliderTextSize.setValue(currentPref.getTaillePolice());
        lblTextSize.setText(currentPref.getTaillePolice() + "px");
        
        sliderLetterSpacing.setValue(currentPref.getEspacementLettres());
        sliderWordSpacing.setValue(currentPref.getEspacementMots());
        sliderLineSpacing.setValue(currentPref.getInterligne());
        
        checkSyllables.setSelected(currentPref.isModeSyllabique());
        checkColorSyllables.setSelected(currentPref.isColorationSyllabes());
        
        comboFont.setValue(currentPref.getTypePolice());
        
        checkFocus.setSelected(currentPref.isFocusLigne());
        
        sliderAudioSpeed.setValue(currentPref.getVitesseAudio());
        lblAudioSpeed.setText(currentPref.getVitesseAudio() + "x");
    }

    @FXML
    void onToggleMode(ActionEvent event) {
        notifyUpdate();
    }

    @FXML
    void onSettingsChanged(ActionEvent event) {
        if (currentPref != null) {
            currentPref.setModeSyllabique(checkSyllables.isSelected());
            currentPref.setColorationSyllabes(checkColorSyllables.isSelected());
            currentPref.setTypePolice(comboFont.getValue());
            currentPref.setFocusLigne(checkFocus.isSelected());
            notifyUpdate();
        }
    }

    @FXML
    void setTheme(ActionEvent event) {
        Button b = (Button) event.getSource();
        String color = (String) b.getUserData();
        if (currentPref != null) {
            currentPref.setCouleurFond(color);
            // Contraster la couleur du texte si nécessaire
            if (color.equals("#334155") || color.equals("#0F172A")) {
                currentPref.setCouleurTexte("#F8FAFC");
            } else {
                currentPref.setCouleurTexte("#1E293B");
            }
            notifyUpdate();
        }
    }

    private void notifyUpdate() {
        if (onUpdate != null && currentPref != null) {
            onUpdate.accept(currentPref);
            // Auto-save in background
            new Thread(() -> dyslexiaService.savePreferences(currentPref)).start();
        }
    }

    public boolean isModeActive() {
        return btnToggleMode.isSelected();
    }
}
