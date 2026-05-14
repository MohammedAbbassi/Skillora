package com.skillora.controllers;

import com.skillora.entities.CommandeRecord;
import com.skillora.services.OrderService;
import com.skillora.services.InvoiceService;
import com.skillora.services.UserService;
import com.skillora.MoneyFormat;
import com.skillora.Session;
import com.skillora.model.OrderLine;
import com.skillora.model.OrderStatsSummary;
import com.skillora.shop.ShopOrdersApp;
import com.skillora.shop.dialog.NewOrderDialogController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class OrdersController {

    private static final double BAR_TRACK_WIDTH = 260.0;

    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox adminCommandsBox;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private TextField adminTotalField;
    @FXML
    private ListView<CommandeRecord> orderList;
    @FXML
    private Button btnInvoice;
    @FXML
    private Label orderMessage;
    @FXML
    private Label statOrderCount;
    @FXML
    private Label statTotalAmount;
    @FXML
    private Label statStatusBreakdown;
    @FXML
    private HBox statusBarRow;
    @FXML
    private Label legendAttente;
    @FXML
    private Label legendPayee;
    @FXML
    private Label legendAnnulee;

    private final OrderService orderService = new OrderService();
    private final InvoiceService invoiceService = new InvoiceService();
    private final UserService userService = new UserService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        if (Session.isAdmin()) {
            subtitleLabel.setText("Toutes les commandes (administration)");
            adminCommandsBox.setVisible(true);
            adminCommandsBox.setManaged(true);
            statusCombo.setItems(FXCollections.observableArrayList("EN_ATTENTE", "PAYEE", "ANNULEE"));
            statusCombo.getSelectionModel().selectFirst();

            orderList.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
                boolean hasSelection = sel != null;
                btnInvoice.setDisable(!hasSelection);
                if (sel != null) {
                    adminTotalField.setText(String.format("%.2f", sel.getTotal()));
                    String st = sel.getStatut();
                    if (st != null && statusCombo.getItems().contains(st)) {
                        statusCombo.getSelectionModel().select(st);
                    }
                }
            });
        } else {
            subtitleLabel.setText("Vos commandes");
            orderList.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
                btnInvoice.setDisable(sel == null);
            });
        }

        orderList.setCellFactory(lv -> new ListCell<CommandeRecord>() {
            @Override
            protected void updateItem(CommandeRecord c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox mainBox = new HBox(15);
                    mainBox.setAlignment(Pos.CENTER_LEFT);
                    mainBox.setPadding(new Insets(10));
                    mainBox.setStyle("-fx-background-color: transparent; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                    VBox infoBox = new VBox(5);
                    HBox.setHgrow(infoBox, Priority.ALWAYS);
                    
                    String dt = c.getDateCommande() == null ? "Date Inconnue" : FMT.format(c.getDateCommande());
                    Label dateLbl = new Label("Commande du " + dt);
                    dateLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
                    
                    String user = Session.isAdmin() ? "Utilisateur ID: " + c.getIdUtilisateur() : "";
                    Label userLbl = new Label(user);
                    userLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                    
                    if (user.isEmpty()) {
                        infoBox.getChildren().add(dateLbl);
                    } else {
                        infoBox.getChildren().addAll(dateLbl, userLbl);
                    }

                    Label statusLbl = new Label(c.getStatut());
                    String statusColor = "#666";
                    if ("PAYEE".equals(c.getStatut())) statusColor = "#27ae60";
                    else if ("EN_ATTENTE".equals(c.getStatut())) statusColor = "#f39c12";
                    else if ("ANNULEE".equals(c.getStatut())) statusColor = "#e74c3c";
                    
                    statusLbl.setStyle(String.format("-fx-font-weight: bold; -fx-text-fill: %s; -fx-font-size: 12px; -fx-padding: 3px 8px; -fx-background-color: #f5f5f5; -fx-background-radius: 10px;", statusColor));

                    Label totalLbl = new Label(MoneyFormat.amount(c.getTotal()));
                    totalLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-min-width: 100px; -fx-alignment: center-right;");
                    
                    mainBox.getChildren().addAll(infoBox, statusLbl, totalLbl);

                    setText(null);
                    setGraphic(mainBox);
                }
            }
        });
        refresh();
    }

    public void refresh() {
        orderMessage.setText("");
        try {
            List<CommandeRecord> list;
            if (Session.isAdmin()) {
                list = orderService.listAll();
            } else {
                long uid = Session.getUser().getIdUtilisateur();
                list = orderService.listForUser(uid);
            }
            orderList.setItems(FXCollections.observableArrayList(list));

            OrderStatsSummary stats = Session.isAdmin()
                    ? orderService.statsAll()
                    : orderService.statsForUser(Session.getUser().getIdUtilisateur());
            applyStats(stats);
        } catch (Exception e) {
            orderMessage.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void onSaveOrderEdits() {
        if (!Session.isAdmin()) {
            return;
        }
        CommandeRecord sel = orderList.getSelectionModel().getSelectedItem();
        String st = statusCombo.getSelectionModel().getSelectedItem();
        if (sel == null || st == null) {
            orderMessage.setText("Sélectionnez une commande.");
            return;
        }
        try {
            String ts = adminTotalField.getText() != null ? adminTotalField.getText().trim().replace(",", ".") : "";
            double total = Double.parseDouble(ts);
            orderService.updateCommandeAdmin(sel.getIdCommande(), st, total);
            refresh();
            orderMessage.setText("Commande mise à jour.");
        } catch (NumberFormatException e) {
            orderMessage.setText("Total invalide (nombre attendu).");
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
        }
    }

    @FXML
    private void onDeleteOrder() {
        if (!Session.isAdmin()) {
            return;
        }
        CommandeRecord sel = orderList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            orderMessage.setText("Sélectionnez une commande.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la commande ?");
        confirm.setContentText("Les lignes associées seront aussi supprimées.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            orderService.deleteOrder(sel.getIdCommande());
            refresh();
            orderMessage.setText("Commande supprimée.");
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
        }
    }

    @FXML
    private void onNewOrder() {
        if (!Session.isAdmin()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(ShopOrdersApp.class.getResource("/fxml/shop/NewOrderDialog.fxml"));
            Parent root = loader.load();
            NewOrderDialogController ctrl = loader.getController();
            ctrl.setOnSuccess(this::refresh);
            Stage dlg = new Stage();
            dlg.initOwner(orderList.getScene().getWindow());
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.setTitle("Nouvelle commande");
            dlg.setScene(new Scene(root));
            dlg.showAndWait();
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
        }
    }

    @FXML
    private void onGenerateInvoice() {
        CommandeRecord sel = orderList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            orderMessage.setText("Sélectionnez une commande.");
            return;
        }
        try {
            // Récupérer les lignes de la commande
            List<OrderLine> lines = orderService.listLines(sel.getIdCommande());
            
            // Récupérer l'utilisateur (si admin, il faut peut-être le charger depuis la base)
            com.skillora.entities.User orderUser;
            if (Session.isAdmin()) {
                // Pour l'admin, on charge l'utilisateur propriétaire de la commande
                orderUser = userService.getAll().stream()
                        .filter(u -> u.getIdUtilisateur() == sel.getIdUtilisateur())
                        .findFirst()
                        .orElse(Session.getUser());
            } else {
                orderUser = Session.getUser();
            }

            // Génération manuelle
            invoiceService.generateAndOpenInvoice(sel.getIdCommande(), orderUser, lines);
            orderMessage.setText("Facture générée avec succès.");
        } catch (Exception e) {
            orderMessage.setText("Erreur génération facture : " + e.getMessage());
        }
    }

    private void applyStats(OrderStatsSummary s) {
        statOrderCount.setText(String.valueOf(s.getOrderCount()));
        statTotalAmount.setText(MoneyFormat.amount(s.getTotalAmount()));
        statStatusBreakdown.setText(String.format(
                "%d / %d / %d",
                s.getEnAttente(), s.getPayee(), s.getAnnulee()));

        legendAttente.setText("EN_ATTENTE " + s.getEnAttente());
        legendPayee.setText("PAYEE " + s.getPayee());
        legendAnnulee.setText("ANNULEE " + s.getAnnulee());

        statusBarRow.getChildren().clear();
        int total = s.getEnAttente() + s.getPayee() + s.getAnnulee();
        if (total <= 0) {
            Region placeholder = new Region();
            placeholder.setPrefHeight(28);
            placeholder.prefWidthProperty().bind(statusBarRow.widthProperty());
            placeholder.getStyleClass().add("order-bar-empty");
            statusBarRow.getChildren().add(placeholder);
            return;
        }
        appendBarSegment(s.getEnAttente(), total, "order-bar-attente");
        appendBarSegment(s.getPayee(), total, "order-bar-payee");
        appendBarSegment(s.getAnnulee(), total, "order-bar-annulee");
    }

    private void appendBarSegment(int count, int total, String styleClass) {
        if (count <= 0) {
            return;
        }
        double w = BAR_TRACK_WIDTH * count / (double) total;
        Region r = new Region();
        r.setMinHeight(28);
        r.setPrefHeight(28);
        r.setMinWidth(Math.max(1, w));
        r.setPrefWidth(Math.max(1, w));
        r.setMaxHeight(28);
        r.getStyleClass().add(styleClass);
        statusBarRow.getChildren().add(r);
    }

    @FXML
    private void onDetail() {
        CommandeRecord sel = orderList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            orderMessage.setText("Sélectionnez une commande.");
            return;
        }
        try {
            List<OrderLine> lines = orderService.listLines(sel.getIdCommande());
            StringBuilder sb = new StringBuilder();
            for (OrderLine ol : lines) {
                sb.append(ol.getNomProduit()).append(" × ").append(ol.getQuantite())
                        .append(" @ ").append(MoneyFormat.amount(ol.getPrixUnitaire())).append(" → ")
                        .append(MoneyFormat.amount(ol.getSousTotal())).append("\n");
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Détails de la commande");
            a.setContentText(sb.length() == 0 ? "Aucune ligne." : sb.toString());
            a.showAndWait();
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
        }
    }
}
