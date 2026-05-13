package controllers;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import entities.UserPreferences;
import services.ServiceUserPreferences;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * AccessibilityController - Handles dynamic UI adaptation based on user preferences
 * Applies: font family, font size, line spacing, letter spacing, colors, and reduced animations
 */
public class AccessibilityController implements Initializable {

    private static UserPreferences currentPreferences;
    private static ServiceUserPreferences prefsService = new ServiceUserPreferences();

    @FXML private StackPane rootPane;
    @FXML private VBox mainContent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load user preferences if logged in
        if (currentPreferences != null) {
            applyPreferences(currentPreferences);
        } else {
            // Apply default accessibility settings
            applyDefaultSettings();
        }
    }

    /**
     * Apply loaded preferences to the entire scene
     */
    public void applyPreferences(UserPreferences prefs) {
        if (prefs == null) {
            applyDefaultSettings();
            return;
        }

        currentPreferences = prefs;
        Scene scene = rootPane != null ? rootPane.getScene() : getSceneFromRoot();

        if (scene != null) {
            applyFontFamily(scene, prefs.getTypePolice());
            applyFontSize(scene, prefs.getTaillePolice());
            applyLineSpacing(scene, prefs.getInterligne());
            applyLetterSpacing(scene, prefs.getEspacementLettres());
            applyColors(scene, prefs.getCouleurFond(), prefs.getCouleurTexte());
            applyReducedAnimations(scene, prefs.isReduireAnimations());
        }
    }

    /**
     * Apply default accessibility-friendly settings
     */
    public void applyDefaultSettings() {
        Scene scene = getSceneFromRoot();
        if (scene != null) {
            applyFontFamily(scene, "OPENDYSLEXIC".equals(currentPreferences.getTypePolice()) ? "OpenDyslexic" : "Segoe UI");
            applyFontSize(scene, currentPreferences != null ? currentPreferences.getTaillePolice() : 16);
            applyLineSpacing(scene, currentPreferences != null ? currentPreferences.getInterligne() : 1.5);
            applyLetterSpacing(scene, currentPreferences != null ? currentPreferences.getEspacementLettres() : 0);
            applyReducedAnimations(scene, currentPreferences != null && currentPreferences.isReduireAnimations());
        }
    }

    private Scene getSceneFromRoot() {
        if (rootPane != null) {
            return rootPane.getScene();
        }
        return null;
    }

    /**
     * Apply font family dynamically
     */
    private void applyFontFamily(Scene scene, String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty()) {
            fontFamily = "Segoe UI";
        }

        String cssFont = convertFontToCSS(fontFamily);

        ObservableList<String> stylesheets = scene.getStylesheets();
        
        // Remove existing font overrides
        stylesheets.removeIf(s -> s.contains("font-"));

        // Apply new font family via inline style on root
        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            String baseFont = "font-family: \"" + cssFont + "\", \"Segoe UI\", \"System\";";
            parent.setStyle(parent.getStyle() + ";" + baseFont);
        }
    }

    private String convertFontToCSS(String fontName) {
        switch (fontName.toUpperCase()) {
            case "OPENDYSLEXIC":
                return "OpenDyslexic";
            case "ARIAL":
                return "Arial";
            case "VERDANA":
                return "Verdana";
            case "LEXIE_READABLE":
                return "Lexie Readable";
            default:
                return "Segoe UI";
        }
    }

    /**
     * Apply font size scaling
     */
    private void applyFontSize(Scene scene, int size) {
        if (size < 12) size = 12;
        if (size > 32) size = 32;

        double scaleFactor = size / 16.0;

        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.removeIf(s -> s.contains("font-scale"));

        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            String style = parent.getStyle();
            String scaleStyle = String.format("-fx-font-size: %dpx;", size);
            parent.setStyle(style + ";" + scaleStyle);

            // Scale all children proportionally
            scaleNodeFonts(parent, scaleFactor);
        }
    }

    private void scaleNodeFonts(Node node, double scaleFactor) {
        if (node instanceof Label) {
            Label label = (Label) node;
            double originalSize = getBaseFontSize(label.getStyle());
            label.setStyle(label.getStyle() + String.format("-fx-font-size: %.1fpx;", originalSize * scaleFactor));
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                scaleNodeFonts(child, scaleFactor);
            }
        }
    }

    private double getBaseFontSize(String style) {
        if (style == null) return 16;
        try {
            int index = style.indexOf("-fx-font-size:");
            if (index >= 0) {
                String sub = style.substring(index + 14);
                sub = sub.replaceAll("[^0-9.].*", "");
                return Double.parseDouble(sub);
            }
        } catch (Exception e) {
            // ignore
        }
        return 16;
    }

    /**
     * Apply line spacing
     */
    private void applyLineSpacing(Scene scene, double spacing) {
        if (spacing < 1.0) spacing = 1.0;
        if (spacing > 2.5) spacing = 2.5;

        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            String style = parent.getStyle();
            String lineSpacing = String.format("-fx-line-spacing: %.1f;", spacing);
            parent.setStyle(style + ";" + lineSpacing);
        }
    }

    /**
     * Apply letter spacing
     */
    private void applyLetterSpacing(Scene scene, double spacing) {
        if (spacing < 0) spacing = 0;
        if (spacing > 0.5) spacing = 0.5;

        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            String style = parent.getStyle();
            String letterSpacing = String.format("-fx-letter-spacing: %.2f;", spacing);
            parent.setStyle(style + ";" + letterSpacing);
        }
    }

    /**
     * Apply background and text colors
     */
    private void applyColors(Scene scene, String bgColor, String textColor) {
        if (bgColor == null) bgColor = "#FDF6E3";
        if (textColor == null) textColor = "#333333";

        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            String style = parent.getStyle();
            
            String bgStyle = String.format("-fx-background-color: %s;", bgColor);
            String textStyle = String.format("-fx-text-fill: %s;", textColor);
            
            parent.setStyle(style + ";" + bgStyle);
        }
    }

    /**
     * Apply reduced animations setting
     */
    private void applyReducedAnimations(Scene scene, boolean reduced) {
        Node root = scene.getRoot();
        if (root instanceof Parent) {
            Parent parent = (Parent) root;
            if (reduced) {
                parent.getStyleClass().add("reduce-motion");
                parent.setStyle(parent.getStyle() + ";-fx-animations: disabled;");
            } else {
                parent.getStyleClass().remove("reduce-motion");
            }
        }
    }

    /**
     * Toggle between normal and dyslexia-friendly font
     */
    public void toggleDyslexiaMode() {
        if (currentPreferences == null) {
            currentPreferences = new UserPreferences();
        }

        boolean isDyslexia = "OPENDYSLEXIC".equalsIgnoreCase(currentPreferences.getTypePolice());
        currentPreferences.setTypePolice(isDyslexia ? "ARIAL" : "OPENDYSLEXIC");

        applyPreferences(currentPreferences);
    }

    /**
     * Increase font size
     */
    public void increaseFontSize() {
        if (currentPreferences == null) {
            currentPreferences = new UserPreferences();
        }

        int newSize = Math.min(currentPreferences.getTaillePolice() + 2, 32);
        currentPreferences.setTaillePolice(newSize);

        applyPreferences(currentPreferences);
    }

    /**
     * Decrease font size
     */
    public void decreaseFontSize() {
        if (currentPreferences == null) {
            currentPreferences = new UserPreferences();
        }

        int newSize = Math.max(currentPreferences.getTaillePolice() - 2, 12);
        currentPreferences.setTaillePolice(newSize);

        applyPreferences(currentPreferences);
    }

    /**
     * Save current preferences to database
     */
    public void savePreferences() {
        if (currentPreferences != null) {
            try {
                // Update in database
                if (currentPreferences.getIdPreference() > 0) {
                    prefsService.update(currentPreferences);
                } else {
                    prefsService.add(currentPreferences);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Static method to set current user preferences from login
     */
    public static void setUserPreferences(UserPreferences prefs) {
        currentPreferences = prefs;
    }

    /**
     * Static method to get current preferences
     */
    public static UserPreferences getUserPreferences() {
        return currentPreferences;
    }
}