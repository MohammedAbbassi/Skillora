package com.skillora.shop;

import Entities.User;
import Services.UserService;
import com.skillora.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void onLogin() {
        hideError();
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        if (email.isEmpty() || password.isEmpty()) {
            showError("Renseignez l'e-mail et le mot de passe.");
            return;
        }
        try {
            User user = userService.authenticate(email, password);
            if (user == null) {
                showError("Identifiants incorrects ou compte inactif.");
                return;
            }
            Session.setUser(user);
            Parent root = FXMLLoader.load(ShopOrdersApp.class.getResource("/fxml/shop/ShopShell.fxml"));
            ShopOrdersApp.getPrimaryStage().getScene().setRoot(root);
        } catch (SQLException e) {
            showError("Erreur base de données : " + e.getMessage());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
