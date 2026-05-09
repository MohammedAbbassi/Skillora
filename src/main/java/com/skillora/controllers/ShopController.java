package com.skillora.controllers;

import Entities.Produit;
import Services.OrderService;
import Services.ProduitCRUD;
import Services.ProductCatalogStatsService;
import com.skillora.MoneyFormat;
import com.skillora.Session;
import com.skillora.model.CartLine;
import com.skillora.model.CatalogStats;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ShopController {

    @FXML
    private FlowPane productsFlow;
    @FXML
    private ComboBox<String> filterTypeCombo;
    @FXML
    private Label statTotalProducts;
    @FXML
    private Label statAvgPrice;
    @FXML
    private Label statMinMax;
    @FXML
    private Label statByType;
    @FXML
    private ListView<CartLine> cartList;
    @FXML
    private Label cartTotalLabel;
    @FXML
    private Label shopMessage;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final OrderService orderService = new OrderService();
    private final ProductCatalogStatsService catalogStatsService = new ProductCatalogStatsService();

    private static final String FILTER_ALL = "Tous les types";

    private List<Produit> allProducts = List.of();
    private Produit selectedProduct;

    @FXML
    private void initialize() {
        if (Session.isAdmin()) {
            shopMessage.setText("Compte administrateur : l’achat en ligne n’est pas disponible. Gérez les articles dans l’onglet « Produits ».");
        }

        filterTypeCombo.setOnAction(e -> rebuildProductCards());

        cartList.setItems(Session.getCart());
        cartList.setCellFactory(lv -> new ListCell<>() {
            private final Label name = new Label();
            private final Label subtotal = new Label();
            private final Spinner<Integer> qSpinner = new Spinner<>();
            private final Button removeBtn = new Button("✕");
            private final Region spacer = new Region();
            private final HBox root = new HBox(10, name, spacer, qSpinner, subtotal, removeBtn);

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                subtotal.getStyleClass().add("muted");
                removeBtn.getStyleClass().add("icon-button-danger");
                removeBtn.setFocusTraversable(false);

                qSpinner.setEditable(true);
                qSpinner.setPrefWidth(86);
                qSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 999, 1));

                removeBtn.setOnAction(e -> {
                    CartLine line = getItem();
                    if (!Session.isAdmin() && line != null) {
                        Session.removeLine(line);
                        updateTotal();
                    }
                });

                qSpinner.valueProperty().addListener((obs, oldV, newV) -> {
                    CartLine line = getItem();
                    if (Session.isAdmin() || line == null || newV == null) {
                        return;
                    }
                    line.setQuantite(newV);
                    subtotal.setText(MoneyFormat.amount(line.getSousTotal()));
                    updateTotal();
                });
            }

            @Override
            protected void updateItem(CartLine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                name.setText(item.getNom());
                subtotal.setText(MoneyFormat.amount(item.getSousTotal()));
                qSpinner.getValueFactory().setValue(Math.max(1, item.getQuantite()));
                qSpinner.setDisable(Session.isAdmin());
                removeBtn.setDisable(Session.isAdmin());
                setGraphic(root);
            }
        });
        Session.getCart().addListener((javafx.collections.ListChangeListener<CartLine>) c -> updateTotal());
        updateTotal();

        loadProductsAndStats();
    }

    private void loadProductsAndStats() {
        shopMessage.setText("");
        try {
            allProducts = produitCRUD.afficher();
            Collections.sort(allProducts, Comparator.comparing(Produit::getNom, Comparator.nullsLast(String::compareToIgnoreCase)));

            Set<String> types = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (Produit p : allProducts) {
                if (p.getCategorie() != null) {
                    types.add(p.getCategorie().name());
                }
            }
            List<String> comboItems = new ArrayList<>();
            comboItems.add(FILTER_ALL);
            comboItems.addAll(types);
            filterTypeCombo.setItems(FXCollections.observableArrayList(comboItems));
            filterTypeCombo.getSelectionModel().selectFirst();

            CatalogStats stats = catalogStatsService.loadStats();
            statTotalProducts.setText(String.valueOf(stats.getTotalProducts()));
            statAvgPrice.setText(MoneyFormat.amount(stats.getAvgPrice()));
            statMinMax.setText(String.format(
                    "%.2f / %.2f TND",
                    stats.getMinPrice(),
                    stats.getMaxPrice()));
            if (stats.getCountByType().isEmpty()) {
                statByType.setText("—");
            } else {
                String joined = stats.getCountByType().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining(" · "));
                statByType.setText(joined);
            }

            rebuildProductCards();
        } catch (Exception e) {
            shopMessage.setText("Erreur chargement produits : " + e.getMessage());
            productsFlow.getChildren().clear();
        }
    }

    private void rebuildProductCards() {
        productsFlow.getChildren().clear();
        selectedProduct = null;
        String filter = filterTypeCombo.getSelectionModel().getSelectedItem();
        for (Produit p : allProducts) {
            if (filter != null && !FILTER_ALL.equals(filter)
                    && (p.getCategorie() == null || !filter.equalsIgnoreCase(p.getCategorie().name()))) {
                continue;
            }
            productsFlow.getChildren().add(buildProductCard(p));
        }
    }

    private VBox buildProductCard(Produit p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);
        card.setMinWidth(240);
        card.setMaxWidth(300);
        card.setPadding(new Insets(0)); // On gère le padding différemment pour l'image

        // Conteneur de l'image
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefHeight(160);
        imgContainer.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 14 14 0 0; -fx-overflow: hidden;");
        
        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(160);
        iv.setPreserveRatio(true);
        
        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size: 40px; -fx-opacity: 0.2;");
        
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String path = p.getImage();
                if (path.contains("pollinations.ai/p/")) {
                    path = path.replace("pollinations.ai/p/", "image.pollinations.ai/prompt/");
                }
                
                Image img;
                if (path.startsWith("http")) {
                    img = new Image(path, true);
                } else {
                    img = new Image(new File(path).toURI().toString());
                }
                iv.setImage(img);
                placeholder.setVisible(false);
            } catch (Exception e) {
                // Image par défaut
            }
        }
        imgContainer.getChildren().addAll(placeholder, iv);

        // Contenu texte (avec padding)
        VBox content = new VBox(8);
        content.setPadding(new Insets(14));
        VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

        Label name = new Label(p.getNom());
        name.getStyleClass().add("course-card-title");
        name.setWrapText(true);

        Button quickAdd = new Button("＋");
        quickAdd.getStyleClass().add("icon-button");
        quickAdd.setFocusTraversable(false);
        quickAdd.setTooltip(new Tooltip("Ajouter au panier"));
        quickAdd.setOnAction(e -> {
            shopMessage.setText("");
            if (Session.isAdmin()) {
                shopMessage.setText("Les administrateurs ne peuvent pas acheter. Utilisez l’onglet « Produits ».");
                return;
            }
            Session.addOrMergeToCart(p, 1);
            cartList.refresh();
            updateTotal();
            shopMessage.setText("Ajouté au panier : " + p.getNom());
        });

        HBox titleRow = new HBox(10, name, new Region(), quickAdd);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        Label typePrice = new Label(str(p.getCategorie() == null ? "" : p.getCategorie().name()) + " · " + MoneyFormat.amount(p.getPrix()));
        typePrice.getStyleClass().add("course-card-meta");

        String desc = str(p.getDescription());
        if (desc.length() > 100) {
            desc = desc.substring(0, 100) + "…";
        }
        Label excerpt = new Label(desc.isEmpty() ? "—" : desc);
        excerpt.setWrapText(true);
        excerpt.getStyleClass().add("muted");

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button selectBtn = new Button("Sélectionner");
        selectBtn.getStyleClass().add("secondary-button");
        selectBtn.setMaxWidth(Double.MAX_VALUE);
        selectBtn.setOnAction(e -> {
            selectedProduct = p;
            for (var n : productsFlow.getChildren()) {
                n.getStyleClass().remove("product-card-selected");
            }
            card.getStyleClass().add("product-card-selected");
            shopMessage.setText("Sélection : " + p.getNom());
        });

        content.getChildren().addAll(titleRow, typePrice, excerpt, spacer, selectBtn);
        card.getChildren().addAll(imgContainer, content);
        return card;
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }

    private void updateTotal() {
        double t = Session.getCart().stream().mapToDouble(CartLine::getSousTotal).sum();
        cartTotalLabel.setText("Total : " + MoneyFormat.amount(t));
    }

    @FXML
    private void onRemoveLine() {
        if (Session.isAdmin()) {
            return;
        }
        CartLine line = cartList.getSelectionModel().getSelectedItem();
        if (line != null) {
            Session.removeLine(line);
        }
        updateTotal();
    }

    @FXML
    private void onCheckout() {
        shopMessage.setText("");
        if (Session.isAdmin()) {
            shopMessage.setText("Les administrateurs ne peuvent pas passer commande depuis la boutique.");
            return;
        }
        if (!Session.isLoggedIn()) {
            shopMessage.setText("Session invalide.");
            return;
        }
        if (Session.getCart().isEmpty()) {
            shopMessage.setText("Le panier est vide.");
            return;
        }
        try {
            long uid = Session.getUser().getIdUtilisateur();
            var copy = List.copyOf(Session.getCart());
            orderService.placeOrder(uid, copy, "EN_ATTENTE");
            Session.getCart().clear();
            updateTotal();
            shopMessage.setText("Commande enregistrée avec succès (statut EN_ATTENTE).");
        } catch (Exception e) {
            shopMessage.setText("Erreur : " + e.getMessage());
        }
    }
}
