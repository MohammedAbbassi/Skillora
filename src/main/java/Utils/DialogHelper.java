package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class DialogHelper {

    private DialogHelper() {}

    public static boolean showConfirm(Window owner, String title, String message) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(title);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 24;");
        root.setPrefWidth(360);

        Label msg = new Label(message);
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-wrap-text: true;");

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");

        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");

        final boolean[] result = new boolean[1];

        cancelBtn.setOnAction(e -> { result[0] = false; stage.close(); });
        okBtn.setOnAction(e -> { result[0] = true; stage.close(); });

        buttons.getChildren().addAll(cancelBtn, okBtn);
        root.getChildren().addAll(msg, buttons);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();

        return result[0];
    }

    public static void showInfo(Window owner, String title, String content) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(title);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 24;");
        root.setPrefWidth(400);

        Label msg = new Label(content);
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-wrap-text: true;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        root.getChildren().addAll(msg, btnRow);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
