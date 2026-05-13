package controllers;

import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import services.ServiceUser;
import utils.CountryService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> firstNameCol;
    @FXML private TableColumn<User, String> lastNameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Boolean> statusCol;
    @FXML private TableColumn<User, Void> actionsCol;

    @FXML private TextField searchField;
    @FXML private Label totalUsersLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUsers();
        setupSearch();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("estActif"));

        // Custom cell for Status (Active/Inactive)
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Active" : "Inactive");
                    setTextFill(item ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Role");
                    private final Button toggleBtn = new Button("Status");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, toggleBtn, deleteBtn);

                    {
                        editBtn.getStyleClass().add("action-btn-small");
                        toggleBtn.getStyleClass().add("action-btn-small");
                        deleteBtn.getStyleClass().addAll("action-btn-small", "delete-btn");

                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleEditRole(user);
                        });

                        toggleBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleToggleStatus(user);
                        });

                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDelete(user);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        actionsCol.setCellFactory(cellFactory);
    }

    private void loadUsers() {
        try {
            userList.setAll(serviceUser.getAll());
            totalUsersLabel.setText("Total Users: " + userList.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load users.");
        }
    }

    private void setupSearch() {
        FilteredList<User> filteredData = new FilteredList<>(userList, p -> true);
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
        });
        userTable.setItems(filteredData);
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
            javafx.application.Platform.runLater(() -> countryBox.getItems().setAll(countries));
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
            showAlert(Alert.AlertType.ERROR, "Add Failed", e.getMessage());
        }
    }

    private void handleEditRole(User user) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(user.getRole(), "ETUDIANT", "INSTRUCTEUR", "ADMIN");
        dialog.setTitle("Change User Role");
        dialog.setHeaderText("Change role for: " + user.getNomUtilisateur());
        dialog.setContentText("Select new role:");

        dialog.showAndWait().ifPresent(newRole -> {
            try {
                serviceUser.updateRole(user.getId(), newRole);
                user.setRole(newRole);
                userTable.refresh();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage());
            }
        });
    }

    private void handleToggleStatus(User user) {
        boolean newStatus = !user.isEstActif();
        try {
            serviceUser.toggleStatus(user.getId(), newStatus);
            user.setEstActif(newStatus);
            userTable.refresh();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage());
        }
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete user: " + user.getNomUtilisateur());
        alert.setContentText("Are you sure? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceUser.delete(user);
                    userList.remove(user);
                    totalUsersLabel.setText("Total Users: " + userList.size());
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
