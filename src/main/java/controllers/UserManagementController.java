package controllers;

import entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import services.ServiceUser;
import utils.CountryService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private VBox userCardsContainer;
    @FXML private TextField searchField;
    @FXML private Label totalUsersLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadUsers();
        setupSearch();
    }

    private void loadUsers() {
        try {
            userList.setAll(serviceUser.getAll());
            totalUsersLabel.setText("Total Users: " + userList.size());
            renderUserCards();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load users.");
        }
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(userList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                if (user.getNomUtilisateur().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getNom() != null && user.getNom().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
            renderUserCards();
        });
    }

    private void renderUserCards() {
        userCardsContainer.getChildren().clear();
        for (User user : filteredData) {
            userCardsContainer.getChildren().add(createUserCard(user));
        }
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox userInfo = new VBox(5);
        Label nameLabel = new Label((user.getPrenom() != null ? user.getPrenom() : "") + " " + (user.getNom() != null ? user.getNom() : ""));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label emailLabel = new Label(user.getEmail() + " (@" + user.getNomUtilisateur() + ")");
        emailLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        userInfo.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        VBox roleInfo = new VBox(5);
        Label roleLabel = new Label(user.getRole().name());
        roleLabel.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label statusLabel = new Label(user.isEstActif() ? "Active" : "Inactive");
        statusLabel.setStyle("-fx-text-fill: " + (user.isEstActif() ? "#16a34a" : "#dc2626") + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        roleInfo.getChildren().addAll(roleLabel, statusLabel);
        roleInfo.setAlignment(Pos.CENTER);
        roleInfo.setMinWidth(100);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button("Role");
        Button toggleBtn = new Button("Status");
        Button deleteBtn = new Button("Delete");
        
        editBtn.getStyleClass().add("action-btn-small");
        toggleBtn.getStyleClass().add("action-btn-small");
        deleteBtn.getStyleClass().addAll("action-btn-small", "delete-btn");

        editBtn.setOnAction(e -> handleEditRole(user));
        toggleBtn.setOnAction(e -> handleToggleStatus(user));
        deleteBtn.setOnAction(e -> handleDelete(user));

        actions.getChildren().addAll(editBtn, toggleBtn, deleteBtn);

        card.getChildren().addAll(userInfo, roleInfo, actions);
        return card;
    }

    private void handleEditRole(User user) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(user.getRole().name(), "ETUDIANT", "INSTRUCTEUR", "ADMIN");
        dialog.setTitle("Change Role");
        dialog.setHeaderText("Update role for " + user.getNomUtilisateur());
        dialog.setContentText("Select new role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            try {
                serviceUser.updateRole(user.getId(), newRole);
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not update role.");
            }
        });
    }

    private void handleToggleStatus(User user) {
        try {
            serviceUser.toggleStatus(user.getId(), !user.isEstActif());
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not toggle status.");
        }
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User: " + user.getNomUtilisateur());
        alert.setContentText("Are you sure you want to delete this user?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.delete(user);
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete user.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleAddUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Create a new user account");

        ButtonType addButtonType = new ButtonType("Add User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ETUDIANT", "INSTRUCTEUR", "ADMIN");
        roleBox.setValue("ETUDIANT");

        ComboBox<String> countryBox = new ComboBox<>();
        countryBox.setPromptText("Select country");
        CountryService.getInstance().getAllCountryNames().thenAccept(countries -> {
            Platform.runLater(() -> countryBox.getItems().setAll(countries));
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("First Name:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleBox, 1, 5);
        grid.add(new Label("Country:"), 0, 6);
        grid.add(countryBox, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != addButtonType) {
                return null;
            }

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String password = passwordField.getText();
            String country = countryBox.getValue();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Username, email, and password are required.");
                return null;
            }

            User newUser = new User();
            newUser.setNomUtilisateur(username);
            newUser.setEmail(email);
            newUser.setPrenom(firstName);
            newUser.setNom(lastName);
            newUser.setMotDePasse(password);
            newUser.setRole(roleBox.getValue());
            newUser.setPays(country);
            newUser.setEstActif(true);
            return newUser;
        });

        Optional<User> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        User newUser = result.get();
        try {
            if (serviceUser.emailExists(newUser.getEmail())) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "This email is already used.");
                return;
            }
            if (serviceUser.usernameExists(newUser.getNomUtilisateur())) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "This username is already used.");
                return;
            }

            serviceUser.add(newUser);
            loadUsers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not add user.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
