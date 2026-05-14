package com.skillora.shop;

import com.skillora.shop.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ShopOrdersApp extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(ShopOrdersApp.class.getResource("/fxml/shop/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 680);
        var url = ShopOrdersApp.class.getResource("/css/theme.css");
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
        stage.setTitle(AppConfig.appTitle());
        stage.setMinWidth(880);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
