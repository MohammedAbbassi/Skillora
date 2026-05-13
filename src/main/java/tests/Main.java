package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.ServiceUser;
import controllers.LoginController;
import javafx.stage.StageStyle;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            new ServiceUser().createAdminIfNotExists();
        } catch (SQLException e) {
            System.err.println("Could not verify/create admin: " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setPrimaryStage(stage);

        // Remove the top bar (Undecorated)
        stage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Make window draggable (since it's undecorated)
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });

        stage.setScene(scene);
        stage.setTitle("Skillora - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}