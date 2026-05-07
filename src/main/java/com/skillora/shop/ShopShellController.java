package com.skillora.shop;

import com.skillora.Session;
import com.skillora.controllers.AdminProductsController;
import com.skillora.controllers.OrdersController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ShopShellController {

    @FXML
    private Label userBadge;
    @FXML
    private TabPane mainTabs;
    @FXML
    private Tab tabShop;
    @FXML
    private Tab tabOrders;
    @FXML
    private Tab tabAdminProducts;
    @FXML
    private OrdersController ordersViewController;
    @FXML
    private AdminProductsController adminProductsController;

    @FXML
    private void initialize() {
        var u = Session.getUser();
        if (u != null) {
            userBadge.setText(u.getPrenom() + " " + u.getNom() + " · " + u.getRole());
        }

        if (Session.isAdmin()) {
            mainTabs.getTabs().remove(tabShop);
            mainTabs.getTabs().remove(tabAdminProducts);
            mainTabs.getTabs().add(0, tabAdminProducts);
        } else {
            mainTabs.getTabs().remove(tabAdminProducts);
        }

        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabOrders && ordersViewController != null) {
                ordersViewController.refresh();
            }
            if (Session.isAdmin() && newTab == tabAdminProducts && adminProductsController != null) {
                adminProductsController.reload();
            }
        });

        if (Session.isAdmin() && adminProductsController != null) {
            adminProductsController.reload();
        }
    }

    @FXML
    private void onLogout() {
        try {
            Session.logout();
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    ShopOrdersApp.class.getResource("/fxml/shop/Login.fxml"));
            ShopOrdersApp.getPrimaryStage().getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
