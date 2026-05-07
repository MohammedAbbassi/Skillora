package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import utils.MyDatabase;

public class DatabaseErrorController {

    @FXML private Label errorLabel;
    @FXML private Button btnRetry;

    @FXML
    public void initialize() {
        String lastError = MyDatabase.getInstance().getLastError();
        if (lastError != null) {
            errorLabel.setText(lastError);
        }
    }

    @FXML
    void onRetry() {
        MyDatabase.getInstance().reconnect();
        MainLayoutController.getInstance().initialize();
    }

}
