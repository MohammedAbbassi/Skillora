package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public class QuizController {

    @FXML private Label quizTitle;

    @FXML
    public void initialize() {
        quizTitle.setText("Quiz Final de Skillora");
    }

    @FXML
    void onBack(ActionEvent event) {
        MainLayoutController.getInstance().loadView("/ui/cours-list.fxml");
    }
}
