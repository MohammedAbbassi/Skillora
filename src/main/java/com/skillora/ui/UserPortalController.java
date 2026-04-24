package com.skillora.ui;

import com.skillora.interfaces.user_interface.IUserService;
import com.skillora.model.user.User;
import com.skillora.service.user_service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserPortalController {

    private final IUserService userService = new UserService();

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private TextField registerUsernameField;

    @FXML
    private TextField registerFirstNameField;

    @FXML
    private TextField registerLastNameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label registerStatusLabel;

    @FXML
    public void onLogin() {
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            loginStatusLabel.setText("Please fill in email and password.");
            return;
        }

        try {
            User user = userService.login(email, password);
            if (user != null) {
                loginStatusLabel.setText("Welcome back, " + user.getPrenom() + "!");
            } else {
                loginStatusLabel.setText("Invalid email or password.");
            }
        } catch (Exception e) {
            loginStatusLabel.setText("Login failed. Check database connection.");
        }
    }

    @FXML
    public void onRegister() {
        String username = registerUsernameField.getText().trim();
        String firstName = registerFirstNameField.getText().trim();
        String lastName = registerLastNameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            registerStatusLabel.setText("All registration fields are required.");
            return;
        }

        try {
            User user = new User(username, lastName, firstName, email, "");
            boolean created = userService.register(user, password);
            if (created) {
                registerStatusLabel.setText("Account created successfully!");
                clearRegisterForm();
            } else {
                registerStatusLabel.setText("Registration failed. Email may already exist.");
            }
        } catch (Exception e) {
            registerStatusLabel.setText("Registration failed. Check database connection.");
        }
    }

    private void clearRegisterForm() {
        registerUsernameField.clear();
        registerFirstNameField.clear();
        registerLastNameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
    }
}
