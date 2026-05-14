package com.skillora.shop.dialog;

import com.skillora.shop.entities.Produit;
import com.skillora.shop.entities.User;
import com.skillora.shop.services.OrderService;
import com.skillora.shop.services.ProduitCRUD;
import com.skillora.shop.services.UserService;
import com.skillora.shop.MoneyFormat;
import com.skillora.shop.model.CartLine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class NewOrderDialogController {

    @FXML
    private ComboBox<User> userCombo;
    @FXML
    private ComboBox<Produit> productCombo;
    @FXML
    private Spinner<Integer> qtySpinner;
    @FXML
    private ListView<CartLine> linesView;
    @FXML
    private Label dialogMsg;

    private final UserService userService = new UserService();
    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final OrderService orderService = new OrderService();

    private final ObservableList<CartLine> lines = FXCollections.observableArrayList();
    private Runnable onSuccess;

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void initialize() {
        qtySpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 999, 1));
        linesView.setItems(lines);

        try {
            List<User> users = userService.getAll();
            userCombo.setItems(FXCollections.observableArrayList(users));
            userCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(User u) {
                    return u == null ? "" : u.getEmail() + " (" + u.getPrenom() + " " + u.getNom() + ")";
                }

                @Override
                public User fromString(String s) {
                    return null;
                }
            });
            if (!users.isEmpty()) {
                userCombo.getSelectionModel().selectFirst();
            }

            List<Produit> products = produitCRUD.afficher();
            productCombo.setItems(FXCollections.observableArrayList(products));
            productCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Produit p) {
                    return p == null ? "" : p.getNom() + " — " + MoneyFormat.amount(p.getPrix());
                }

                @Override
                public Produit fromString(String s) {
                    return null;
                }
            });
            if (!products.isEmpty()) {
                productCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            dialogMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onAddLine() {
        dialogMsg.setText("");
        Produit p = productCombo.getSelectionModel().getSelectedItem();
        if (p == null) {
            dialogMsg.setText("Choisissez un produit.");
            return;
        }
        int q = qtySpinner.getValue() != null ? qtySpinner.getValue() : 1;
        for (CartLine line : lines) {
            if (line.getIdProduit() == p.getId()) {
                line.setQuantite(line.getQuantite() + q);
                linesView.refresh();
                return;
            }
        }
        lines.add(CartLine.fromProduit(p, q));
    }

    @FXML
    private void onConfirm() {
        dialogMsg.setText("");
        User u = userCombo.getSelectionModel().getSelectedItem();
        if (u == null) {
            dialogMsg.setText("Choisissez un client.");
            return;
        }
        if (lines.isEmpty()) {
            dialogMsg.setText("Ajoutez au moins une ligne.");
            return;
        }
        try {
            orderService.placeOrder(u.getIdUtilisateur(), new ArrayList<>(lines), "EN_ATTENTE");
            if (onSuccess != null) {
                onSuccess.run();
            }
            close();
        } catch (Exception e) {
            dialogMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) userCombo.getScene().getWindow();
        s.close();
    }
}
