package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import entities.User;
import entities.UserPreferences;
import services.ServiceUser;
import services.ServiceUserPreferences;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private VBox     loginCard;
    @FXML private Label    formTitle;
    @FXML private Label    formSubtitle;
    @FXML private VBox     nameFieldBox;
    @FXML private TextField nameField;
    @FXML private TextField    emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label    forgotLabel;
    @FXML private Label    errorLabel;
    @FXML private Button   actionBtn;
    @FXML private Label    toggleLabel;
    @FXML private Label    toggleLink;

    private boolean  isLoginMode   = true;
    private Stage    primaryStage;
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceUserPreferences servicePrefs = new ServiceUserPreferences();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cardEntrance();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void cardEntrance() {
        loginCard.setOpacity(0);
        loginCard.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(600), loginCard);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), loginCard);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, tt).play();
    }

    @FXML
    private void onToggleMode() {
        isLoginMode = !isLoginMode;

        FadeTransition out = new FadeTransition(Duration.millis(160), loginCard);
        out.setToValue(0);
        out.setOnFinished(e -> {
            applyModeState();
            FadeTransition in = new FadeTransition(Duration.millis(260), loginCard);
            in.setToValue(1);
            in.play();
        });
        out.play();
    }

    private void applyModeState() {
        hideError();
        if (isLoginMode) {
            formTitle.setText("Welcome back");
            formSubtitle.setText("Sign in to continue learning");
            actionBtn.setText("SIGN IN");
            toggleLabel.setText("Don't have an account?");
            toggleLink.setText("Register");
            forgotLabel.setVisible(true);
            forgotLabel.setManaged(true);
            nameFieldBox.setVisible(false);
            nameFieldBox.setManaged(false);
        } else {
            formTitle.setText("Create account");
            formSubtitle.setText("Join the learning community");
            actionBtn.setText("REGISTER");
            toggleLabel.setText("Already have an account?");
            toggleLink.setText("Sign in");
            forgotLabel.setVisible(false);
            forgotLabel.setManaged(false);
            nameFieldBox.setVisible(true);
            nameFieldBox.setManaged(true);
        }
        emailField.clear();
        passwordField.clear();
        if (nameField != null) nameField.clear();
    }

    @FXML
    public void onCloseApp() {
        System.exit(0);
    }

    @FXML
    private void onActionButton() {
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        // Special bypass for admin:admin
        boolean isAdminBypass = "admin".equalsIgnoreCase(email) && "admin".equals(pass);

        // 1. Basic empty check
        if (email.isEmpty() || pass.isEmpty()) {
            showError("Please fill in all fields.");
            shake(actionBtn);
            return;
        }

        if (!isAdminBypass) {
            // 2. Email format validation
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                showError("Please enter a valid email address.");
                shake(emailField);
                return;
            }

            // 3. Password length check (Registration only)
            if (!isLoginMode && pass.length() < 6) {
                showError("Password must be at least 6 characters.");
                shake(passwordField);
                return;
            }
        }

        // 4. Name validation (Registration only)
        if (!isLoginMode) {
            String name = nameField != null ? nameField.getText().trim() : "";
            if (name.isEmpty()) {
                showError("Please enter your name.");
                shake(nameField);
                return;
            }
            if (name.length() < 3) {
                showError("Name is too short.");
                shake(nameField);
                return;
            }
        }

        actionBtn.setText("Loading...");
        actionBtn.setDisable(true);

        new Thread(() -> {
            try {
                User user;
                if (isLoginMode) {
                    user = serviceUser.login(email, pass);
                    if (user == null) {
                        javafx.application.Platform.runLater(() -> {
                            showError("Invalid email or password.");
                            actionBtn.setText("SIGN IN");
                            actionBtn.setDisable(false);
                            shake(actionBtn);
                        });
                        return;
                    }
                } else {
                    if (serviceUser.emailExists(email)) {
                        javafx.application.Platform.runLater(() -> {
                            showError("Email already registered.");
                            actionBtn.setText("REGISTER");
                            actionBtn.setDisable(false);
                            shake(emailField);
                        });
                        return;
                    }
                    user = new User();
                    user.setNomUtilisateur(email.split("@")[0]);
                    user.setEmail(email);
                    user.setMotDePasse(pass);
                    String name = nameField != null ? nameField.getText().trim() : "";
                    String[] nameParts = name.split(" ", 2);
                    user.setPrenom(nameParts.length > 0 ? nameParts[0] : "");
                    user.setNom(nameParts.length > 1 ? nameParts[1] : "");
                    user.setRole("ETUDIANT");
                    serviceUser.add(user);
                    user = serviceUser.login(email, pass);
                }

                UserPreferences prefs = getUserPreferences(user);
                prefs.setUserName(formatUserName(user));
                prefs.setUserRole(user.getRole());
                prefs.setUserEmail(user.getEmail());
                prefs.setXpPoints(user.getXpPoints());
                prefs.setStreakDays(user.getStreakDays());

                javafx.application.Platform.runLater(() -> {
                    try {
                        loadMainShell(prefs);
                    } catch (Exception ex) {
                        ex.printStackTrace(); // Log the full error to console
                        showError("Failed to load application: " + ex.getMessage());
                        actionBtn.setText(isLoginMode ? "SIGN IN" : "REGISTER");
                        actionBtn.setDisable(false);
                    }
                });
            } catch (SQLException ex) {
                javafx.application.Platform.runLater(() -> {
                    showError("Database error. Please try again.");
                    actionBtn.setText(isLoginMode ? "SIGN IN" : "REGISTER");
                    actionBtn.setDisable(false);
                    ex.printStackTrace();
                });
            }
        }).start();
    }

    private UserPreferences getUserPreferences(User user) {
        try {
            UserPreferences prefs = servicePrefs.getByUserId(user.getId());
            if (prefs != null) {
                prefs.setUserName(formatUserName(user));
                prefs.setUserRole(user.getRole());
                prefs.setUserEmail(user.getEmail());
                return prefs;
            }
        } catch (SQLException e) {
            System.err.println("Could not load preferences: " + e.getMessage());
        }
        return UserPreferences.defaults();
    }

    private String formatUserName(User user) {
        String name = user.getPrenom();
        if (name == null || name.isEmpty()) {
            name = user.getNomUtilisateur();
        }
        if (name == null || name.isEmpty()) {
            name = user.getEmail().split("@")[0];
        }
        return name;
    }

    @FXML
    private void onForgotPassword() {
        shake(emailField);
        emailField.requestFocus();
    }

    private void loadMainShell(UserPreferences prefs) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/MainLayout.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.applyPreferences(prefs);

        // Open a new TRANSPARENT stage for the dashboard (allows rounded corners)
        Stage dashboardStage = new Stage(javafx.stage.StageStyle.TRANSPARENT);
        Scene scene = new Scene(root, 1280, 800);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm());

        dashboardStage.setTitle("Skillora - Dashboard");
        dashboardStage.setScene(scene);
        dashboardStage.setMinWidth(1100);
        dashboardStage.setMinHeight(750);
        
        if (primaryStage != null) {
            primaryStage.close();
        }
        dashboardStage.show();
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

    private void shake(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0); tt.setToX(10);
        tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}