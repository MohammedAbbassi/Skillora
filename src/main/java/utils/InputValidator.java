package utils;

import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

public class InputValidator {

    /**
     * Applique un filtre pour n'autoriser que les lettres et les espaces.
     */
    public static void applyLettersOnly(TextInputControl input) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("[a-zA-Z\\s\\u00C0-\\u017F]*")) {
                return change;
            }
            return null;
        };
        input.setTextFormatter(new TextFormatter<>(filter));
    }
}
