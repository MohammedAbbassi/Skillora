package controllers;

import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Circle;
import services.ServiceUser;
import utils.BadgeUtils;
import utils.CountryService;
import utils.Session;

import java.sql.SQLException;

public class ProfileController {
    @FXML private Circle profileCircle;
    @FXML private Label fullNameLabel;
    @FXML private Label roleBadge;
    @FXML private Label emailDisplayLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private Label badgeCountLabel;
    @FXML private FlowPane achievementsFlow;

    private final ServiceUser serviceUser = new ServiceUser();
    private User user;

    @FXML
    private void initialize() {
        user = Session.getUser();
        if (user == null) {
            setEmptyState();
            return;
        }
        
        loadCountries();
        populateForm();
    }

    private void loadCountries() {
        CountryService.getInstance().getAllCountryNames().thenAccept(countries -> {
            Platform.runLater(() -> {
                countryComboBox.getItems().setAll(countries);
                if (user != null && user.getPays() != null) {

                    // Try to find the matching entry with flag
                    String userCountry = user.getPays();
                    String match = countries.stream()
                            .filter(c -> c.startsWith(userCountry))
                            .findFirst()
                            .orElse(userCountry);
                    countryComboBox.setValue(match);

                    countryComboBox.setValue(user.getPays());

                }
            });
        });
    }

    @FXML
    private void handleSave() {
        if (user == null) {
            showAlert(Alert.AlertType.WARNING, "Profile", "No active user session found.");
            return;
        }

        user.setPrenom(firstNameField.getText().trim());
        user.setNom(lastNameField.getText().trim());
        user.setNomUtilisateur(usernameField.getText().trim());

        
        // Save country without the flag emoji
        String fullCountry = countryComboBox.getValue();
        if (fullCountry != null && fullCountry.contains(" ")) {
            user.setPays(fullCountry.substring(0, fullCountry.lastIndexOf(" ")).trim());
        } else {
            user.setPays(fullCountry);
        }


        try {
            serviceUser.update(user);
            Session.setUser(user);
            populateForm();
            showAlert(Alert.AlertType.INFORMATION, "Profile", "Profile updated successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Profile", "Could not update profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        populateForm();
    }

    @FXML
    private void handleUploadPhoto() {
        showAlert(Alert.AlertType.INFORMATION, "Profile", "Use the main profile page to change your profile photo.");
    }

    private void populateForm() {
        firstNameField.setText(nullToEmpty(user.getPrenom()));
        lastNameField.setText(nullToEmpty(user.getNom()));
        usernameField.setText(nullToEmpty(user.getNomUtilisateur()));
        emailField.setText(nullToEmpty(user.getEmail()));
        if (countryComboBox.getItems().isEmpty()) {
            loadCountries();
        } else {
            countryComboBox.setValue(user.getPays());
        }
        fullNameLabel.setText(formatName(user));
        roleBadge.setText(nullToEmpty(user.getRole().name()));
        emailDisplayLabel.setText(nullToEmpty(user.getEmail()));
        if (achievementsFlow != null) {
            BadgeUtils.buildAchievements(user, achievementsFlow);
            badgeCountLabel.setText(achievementsFlow.getChildren().size() + " / 32 Badges");
        }
    }

    private void setEmptyState() {
        fullNameLabel.setText("Guest");
        roleBadge.setText("GUEST");
        emailDisplayLabel.setText("");
        firstNameField.setDisable(true);
        lastNameField.setDisable(true);
        usernameField.setDisable(true);
        emailField.setDisable(true);
        badgeCountLabel.setText("0 / 32 Badges");
        if (profileCircle != null) {
            profileCircle.setOpacity(0.6);
        }
    }

    private String formatName(User u) {
        String fullName = (nullToEmpty(u.getPrenom()) + " " + nullToEmpty(u.getNom())).trim();
        return fullName.isEmpty() ? nullToEmpty(u.getNomUtilisateur()) : fullName;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
