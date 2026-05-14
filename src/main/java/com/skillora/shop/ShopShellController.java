package com.skillora.shop;

import com.skillora.shop.controllers.AdminProductsController;
import com.skillora.shop.controllers.OrdersController;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ShopShellController {

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
        if (Session.isShopManager()) {
            mainTabs.getTabs().remove(tabShop);
            mainTabs.getTabs().remove(tabAdminProducts);
            mainTabs.getTabs().add(0, tabAdminProducts);
            mainTabs.getSelectionModel().select(tabAdminProducts);
        } else {
            mainTabs.getTabs().remove(tabAdminProducts);
        }

        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabOrders && ordersViewController != null) {
                ordersViewController.refresh();
            }
            if (newTab == tabAdminProducts && adminProductsController != null) {
                adminProductsController.reload();
            }
        });

        if (Session.isShopManager() && adminProductsController != null) {
            adminProductsController.reload();
        }
    }
}
