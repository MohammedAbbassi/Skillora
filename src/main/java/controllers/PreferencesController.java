package controllers;

import entities.UserPreferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceUserPreferences;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

    @FXML private ComboBox<String> fontFamilyCombo;
    @FXML private Slider fontSizeSlider;
    @FXML private Slider lineSpacingSlider;
    @FXML private Slider letterSpacingSlider;
    @FXML private Label fontSizeValue;
    @FXML private Label lineSpacingValue;
    @FXML private Label letterSpacingValue;
    @FXML private ColorPicker bgColorPicker;
    @FXML private ColorPicker textColorPicker;
    @FXML private Label bgColorHex;
    @FXML private Label textColorHex;
    @FXML private CheckBox voiceReadingCheck;
    @FXML private CheckBox highlightTextCheck;
    @FXML private CheckBox reduceAnimationsCheck;
    @FXML private TextArea previewTextArea;

    private Object userId;
    private ServiceUserPreferences servicePrefs = new ServiceUserPreferences();
    private UserPreferences currentPrefs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFontCombo();
        setupSliders();
        setupColorPickers();
        updatePreview();
    }

    public void setUserId(Object userId) {
        this.userId = userId;
        loadPreferences();
    }

    private void setupFontCombo() {
        fontFamilyCombo.getItems().addAll("Segoe UI", "Inter", "OpenDyslexic");
        fontFamilyCombo.setValue("Segoe UI");
        fontFamilyCombo.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    private void setupSliders() {
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int val = (int) Math.round(newVal.doubleValue());
            fontSizeValue.setText(val + "px");
            updatePreview();
        });

        lineSpacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = Math.round(newVal.doubleValue() * 10) / 10.0;
            lineSpacingValue.setText(String.format("%.1f", val));
            updatePreview();
        });

        letterSpacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = Math.round(newVal.doubleValue() * 10) / 10.0;
            letterSpacingValue.setText(String.format("%.1fpx", val));
            updatePreview();
        });
    }

    private void setupColorPickers() {
        bgColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            bgColorHex.setText(toHexString(newVal));
            updatePreview();
        });

        textColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            textColorHex.setText(toHexString(newVal));
            updatePreview();
        });
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    private void updatePreview() {
        String fontFamily = fontFamilyCombo.getValue();
        int fontSize = (int) Math.round(fontSizeSlider.getValue());
        double lineSpacing = lineSpacingSlider.getValue();
        double letterSpacing = letterSpacingSlider.getValue();
        Color bgColor = bgColorPicker.getValue();
        Color textColor = textColorPicker.getValue();

        StringBuilder style = new StringBuilder();
        style.append("-fx-font-family: '").append(fontFamily).append("'; ");
        style.append("-fx-font-size: ").append(fontSize).append("px; ");
        style.append("-fx-text-fill: ").append(toHexString(textColor)).append("; ");
        style.append("-fx-background-color: ").append(toHexString(bgColor)).append("; ");

        previewTextArea.setStyle(style.toString());
    }

    private void loadPreferences() {
        if (userId == null) {
            currentPrefs = UserPreferences.defaults();
            applyToControls(currentPrefs);
            return;
        }

        try {
            UserPreferences prefs = servicePrefs.getByUserId(((Number) userId).longValue());
            if (prefs != null) {
                currentPrefs = prefs;
            } else {
                currentPrefs = UserPreferences.defaults();
            }
        } catch (SQLException e) {
            System.err.println("Error loading preferences: " + e.getMessage());
            currentPrefs = UserPreferences.defaults();
        }

        applyToControls(currentPrefs);
    }

    private void applyToControls(UserPreferences prefs) {
        fontFamilyCombo.setValue(mapFontFamily(prefs.getTypePolice()));
        fontSizeSlider.setValue(prefs.getTaillePolice());
        lineSpacingSlider.setValue(prefs.getInterligne());
        letterSpacingSlider.setValue(prefs.getEspacementLettres());

        bgColorPicker.setValue(Color.web(prefs.getCouleurFond()));
        textColorPicker.setValue(Color.web(prefs.getCouleurTexte()));

        voiceReadingCheck.setSelected(prefs.isSyntheseVocale());
        highlightTextCheck.setSelected(prefs.isSurlignageLecture());
        reduceAnimationsCheck.setSelected(prefs.isReduireAnimations());

        fontSizeValue.setText(prefs.getTaillePolice() + "px");
        lineSpacingValue.setText(String.format("%.1f", prefs.getInterligne()));
        letterSpacingValue.setText(String.format("%.1fpx", prefs.getEspacementLettres()));
        bgColorHex.setText(prefs.getCouleurFond());
        textColorHex.setText(prefs.getCouleurTexte());
    }

    private String mapFontFamily(String typePolice) {
        if (typePolice == null) return "Segoe UI";
        switch (typePolice.toUpperCase()) {
            case "OPENDYSLEXIC": return "OpenDyslexic";
            case "INTER": return "Inter";
            default: return "Segoe UI";
        }
    }

    private String reverseMapFontFamily(String displayName) {
        switch (displayName) {
            case "OpenDyslexic": return "OPENDYSLEXIC";
            case "Inter": return "INTER";
            default: return "SEGOEUI";
        }
    }

    @FXML
    private void onSavePreferences() {
        UserPreferences prefs = new UserPreferences();
        if (userId != null) {
            prefs.setIdUtilisateur(((Number) userId).longValue());
        }
        prefs.setTypePolice(reverseMapFontFamily(fontFamilyCombo.getValue()));
        prefs.setTaillePolice((int) Math.round(fontSizeSlider.getValue()));
        prefs.setInterligne(lineSpacingSlider.getValue());
        prefs.setEspacementLettres(letterSpacingSlider.getValue());
        prefs.setCouleurFond(bgColorHex.getText());
        prefs.setCouleurTexte(textColorHex.getText());
        prefs.setSyntheseVocale(voiceReadingCheck.isSelected());
        prefs.setSurlignageLecture(highlightTextCheck.isSelected());
        prefs.setReduireAnimations(reduceAnimationsCheck.isSelected());

        System.out.println("=== Saving Preferences ===");
        System.out.println("type_police: " + prefs.getTypePolice());
        System.out.println("taille_police: " + prefs.getTaillePolice());
        System.out.println("interligne: " + prefs.getInterligne());
        System.out.println("espacement_lettres: " + prefs.getEspacementLettres());
        System.out.println("couleur_fond: " + prefs.getCouleurFond());
        System.out.println("couleur_texte: " + prefs.getCouleurTexte());
        System.out.println("synthese_vocale: " + prefs.isSyntheseVocale());
        System.out.println("surlignage_lecture: " + prefs.isSurlignageLecture());
        System.out.println("reduire_animations: " + prefs.isReduireAnimations());

        try {
            if (userId != null && servicePrefs.getByUserId(((Number) userId).longValue()) != null) {
                servicePrefs.update(prefs);
            } else if (userId != null) {
                servicePrefs.add(prefs);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Preferences saved successfully!");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save preferences: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onResetDefaults() {
        fontFamilyCombo.setValue("Segoe UI");
        fontSizeSlider.setValue(15);
        lineSpacingSlider.setValue(1.6);
        letterSpacingSlider.setValue(0.0);
        bgColorPicker.setValue(Color.web("#FFFFFF"));
        textColorPicker.setValue(Color.web("#1A1A2E"));
        voiceReadingCheck.setSelected(false);
        highlightTextCheck.setSelected(false);
        reduceAnimationsCheck.setSelected(false);
        updatePreview();
    }

    public static void showPreferencesDialog(Stage owner, Object userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                PreferencesController.class.getResource("/PreferencesView.fxml"));
            Parent root = loader.load();

            PreferencesController controller = loader.getController();
            controller.setUserId(userId);

            Scene scene = new Scene(root, 900, 600);

            Stage dialogStage = new Stage(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.setTitle("Reading Preferences");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner);
            dialogStage.setResizable(false);
            
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to open preferences: " + e.getMessage());
        }
    }
}