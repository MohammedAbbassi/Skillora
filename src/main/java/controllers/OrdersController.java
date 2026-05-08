package controllers;

import entities.CommandeRecord;
import services.OrderService;
import utils.DialogHelper;
import utils.MoneyFormat;
import utils.Session;
import models.OrderLine;
import models.OrderStatsSummary;
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
    private TableView<CommandeRecord> orderTable;
    @FXML
    private TableColumn<CommandeRecord, String> colId;
    @FXML
    private TableColumn<CommandeRecord, String> colDate;
    @FXML
    private TableColumn<CommandeRecord, String> colTotal;
    @FXML
    private TableColumn<CommandeRecord, String> colStatut;
    @FXML
    private TableColumn<CommandeRecord, String> colUser;
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
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        if (Session.isAdmin()) {
            subtitleLabel.setText("Toutes les commandes (administration)");
            adminCommandsBox.setVisible(true);
            adminCommandsBox.setManaged(true);
            statusCombo.setItems(FXCollections.observableArrayList("EN_ATTENTE", "PAYEE", "ANNULEE"));
            statusCombo.getSelectionModel().selectFirst();

            orderTable.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
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
        }

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdCommande())));
        colDate.setCellValueFactory(c -> {
            var dt = c.getValue().getDateCommande();
            return new SimpleStringProperty(dt == null ? "" : FMT.format(dt));
        });
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(MoneyFormat.amount(c.getValue().getTotal())));
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colUser.setCellValueFactory(c -> {
            if (Session.isAdmin()) {
                return new SimpleStringProperty(String.valueOf(c.getValue().getIdUtilisateur()));
            }
            return new SimpleStringProperty("—");
        });

        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refresh();
    }

    public void refresh() {
        orderMessage.setText("");
        try {
            List<CommandeRecord> list;
            if (Session.isAdmin()) {
                list = orderService.listAll();
            } else {
                int uid = Session.getUser().getId();
                list = orderService.listForUser(uid);
            }
            orderTable.setItems(FXCollections.observableArrayList(list));

            OrderStatsSummary stats = Session.isAdmin()
                    ? orderService.statsAll()
                    : orderService.statsForUser(Session.getUser().getId());
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
        CommandeRecord sel = orderTable.getSelectionModel().getSelectedItem();
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
        CommandeRecord sel = orderTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            orderMessage.setText("Sélectionnez une commande.");
            return;
        }
        boolean ok = DialogHelper.showConfirm(
            orderTable.getScene().getWindow(),
            "Supprimer la commande n° " + sel.getIdCommande() + " ?",
            "Les lignes associées seront aussi supprimées.");
        if (!ok) return;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shop/NewOrderDialog.fxml"));
            Parent root = loader.load();
            NewOrderDialogController ctrl = loader.getController();
            ctrl.setOnSuccess(this::refresh);
            Stage dlg = new Stage();
            dlg.initOwner(orderTable.getScene().getWindow());
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.setTitle("Nouvelle commande");
            dlg.setScene(new Scene(root));
            dlg.showAndWait();
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
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
        CommandeRecord sel = orderTable.getSelectionModel().getSelectedItem();
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
            DialogHelper.showInfo(
                orderTable.getScene().getWindow(),
                "Commande #" + sel.getIdCommande(),
                sb.length() == 0 ? "Aucune ligne." : sb.toString());
        } catch (Exception e) {
            orderMessage.setText(e.getMessage());
        }
    }
}
