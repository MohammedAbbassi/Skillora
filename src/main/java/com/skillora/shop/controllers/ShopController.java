package com.skillora.shop.controllers;

import com.skillora.shop.entities.Produit;
import com.skillora.shop.entities.Evaluation;
import com.skillora.shop.entities.Coupon;
import com.skillora.shop.services.OrderService;
import com.skillora.shop.services.ProduitCRUD;
import com.skillora.shop.services.EvaluationCRUD;
import com.skillora.shop.services.InvoiceService;
import com.skillora.shop.services.CouponService;
import com.skillora.shop.services.ProductCatalogStatsService;
import com.skillora.shop.MoneyFormat;
import com.skillora.shop.Session;
import com.skillora.shop.model.CartLine;
import com.skillora.shop.model.OrderLine;
import com.skillora.shop.model.CatalogStats;
import com.skillora.shop.payment.PaymentController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ShopController {

    @FXML
    private FlowPane productsFlow;
    @FXML
    private ComboBox<String> filterTypeCombo;
    @FXML
    private ComboBox<String> sortCombo;
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
    private Label subtotalLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private HBox discountRow;
    @FXML
    private TextField couponField;
    @FXML
    private Label couponStatusMsg;
    @FXML
    private Label shopMessage;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final EvaluationCRUD evaluationCRUD = new EvaluationCRUD();
    private final OrderService orderService = new OrderService();
    private final ProductCatalogStatsService catalogStatsService = new ProductCatalogStatsService();
    private final InvoiceService invoiceService = new InvoiceService();
    private final CouponService couponService = new CouponService();

    private static final String FILTER_ALL = "Tous les types";
    private static final String SORT_NAME = "Trier par nom";
    private static final String SORT_RATING = "Trier par note (décroissant)";

    private List<Produit> allProducts = List.of();
    private Produit selectedProduct;
    private Coupon appliedCoupon;
    private double currentDiscount = 0;

    @FXML
    private void initialize() {
        if (Session.isShopManager()) {
            shopMessage.setText("Compte gestionnaire : l'achat en ligne n'est pas disponible. Gerez les articles dans l'onglet Produits.");
        }

        filterTypeCombo.setOnAction(e -> rebuildProductCards());
        sortCombo.setItems(FXCollections.observableArrayList(SORT_NAME, SORT_RATING));
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(e -> rebuildProductCards());

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
                    if (!Session.isShopManager() && line != null) {
                        Session.removeLine(line);
                        updateTotal();
                    }
                });

                qSpinner.valueProperty().addListener((obs, oldV, newV) -> {
                    CartLine line = getItem();
                    if (Session.isShopManager() || line == null || newV == null) {
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
                qSpinner.setDisable(Session.isShopManager());
                removeBtn.setDisable(Session.isShopManager());
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
        String sort = sortCombo.getSelectionModel().getSelectedItem();

        List<Produit> filteredList = allProducts.stream()
                .filter(p -> filter == null || FILTER_ALL.equals(filter) || (p.getCategorie() != null && filter.equalsIgnoreCase(p.getCategorie().name())))
                .collect(Collectors.toList());

        if (SORT_RATING.equals(sort)) {
            filteredList.sort(Comparator.comparingDouble(Produit::getNoteMoyenne).reversed());
        } else {
            filteredList.sort(Comparator.comparing(Produit::getNom, Comparator.nullsLast(String::compareToIgnoreCase)));
        }

        for (Produit p : filteredList) {
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
        imgContainer.setMinHeight(160);
        imgContainer.setStyle("-fx-background-color: #eef4ff; -fx-background-radius: 14 14 0 0; -fx-overflow: hidden;");
        Rectangle imageClip = new Rectangle(260, 160);
        imageClip.setArcWidth(14);
        imageClip.setArcHeight(14);
        imgContainer.setClip(imageClip);
        
        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(160);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        
        Label placeholder = new Label(initialsForProduct(p));
        placeholder.setTextFill(Color.web("#4f46e5"));
        placeholder.setStyle("-fx-font-size: 34px; -fx-font-weight: 800; -fx-opacity: 0.55;");
        loadProductImage(p, iv, placeholder);

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
            if (Session.isShopManager()) {
                shopMessage.setText("Les gestionnaires ne peuvent pas acheter. Utilisez l'onglet Produits.");
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

        HBox ratingRow = buildStarRating(p);

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

        content.getChildren().addAll(titleRow, typePrice, ratingRow, excerpt, spacer, selectBtn);
        card.getChildren().addAll(imgContainer, content);
        return card;
    }

    private void loadProductImage(Produit p, ImageView imageView, Label placeholder) {
        imageView.setVisible(false);
        String source = p.getImage();
        if (source == null || source.trim().isEmpty()) {
            return;
        }

        try {
            Image img = new Image(resolveImageSource(source), 260, 160, false, true, true);
            img.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    imageView.setVisible(false);
                    placeholder.setVisible(true);
                }
            });
            img.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() >= 1.0 && !img.isError()) {
                    imageView.setImage(img);
                    imageView.setVisible(true);
                    placeholder.setVisible(false);
                }
            });
            if (img.getProgress() >= 1.0 && !img.isError()) {
                imageView.setImage(img);
                imageView.setVisible(true);
                placeholder.setVisible(false);
            }
        } catch (Exception e) {
            imageView.setVisible(false);
            placeholder.setVisible(true);
        }
    }

    private String resolveImageSource(String source) throws Exception {
        String path = source.trim();
        if (path.contains("pollinations.ai/p/")) {
            path = path.replace("pollinations.ai/p/", "image.pollinations.ai/prompt/");
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return URI.create(path.replace(" ", "%20")).toASCIIString();
        }
        if (path.startsWith("file:")) {
            return path;
        }
        return new File(path).toURI().toString();
    }

    private String initialsForProduct(Produit p) {
        String name = p.getNom();
        if (name == null || name.isBlank()) {
            return "IMG";
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
            if (initials.length() == 3) {
                break;
            }
        }
        return initials.length() == 0 ? "IMG" : initials.toString();
    }

    private HBox buildStarRating(Produit p) {
        HBox stars = new HBox(2);
        stars.setAlignment(Pos.CENTER_LEFT);
        
        double rating = p.getNoteMoyenne();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= Math.round(rating) ? "★" : "☆");
            star.setStyle("-fx-font-size: 16px; -fx-text-fill: #f59e0b;");
            
            final int note = i;
            if (!Session.isShopManager() && Session.isLoggedIn()) {
                star.setCursor(javafx.scene.Cursor.HAND);
                star.setOnMouseClicked(e -> rateProduct(p, note));
                star.setOnMouseEntered(e -> star.setStyle("-fx-font-size: 18px; -fx-text-fill: #fbbf24; -fx-cursor: hand;"));
                star.setOnMouseExited(e -> star.setStyle("-fx-font-size: 16px; -fx-text-fill: #f59e0b;"));
            }
            stars.getChildren().add(star);
        }
        
        Label count = new Label(String.format(" (%.1f/5, %d avis)", rating, p.getNombreEvaluations()));
        count.getStyleClass().add("muted");
        count.setStyle("-fx-font-size: 11px; -fx-padding: 0 0 0 5;");
        stars.getChildren().add(count);
        
        return stars;
    }

    private void rateProduct(Produit p, int note) {
        if (Session.isShopManager() || !Session.isLoggedIn()) {
            shopMessage.setText("Vous devez être connecté en tant qu'étudiant pour noter un produit.");
            return;
        }
        try {
            Evaluation e = new Evaluation();
            e.setIdProduit(p.getId());
            e.setIdUtilisateur(Session.getUser().getIdUtilisateur());
            e.setNote(note);
            evaluationCRUD.ajouter(e);
            
            shopMessage.setText("Merci ! Votre note de " + note + " étoiles a été enregistrée.");
            loadProductsAndStats(); // Refresh to show new average
        } catch (Exception ex) {
            shopMessage.setText("Erreur lors de la notation : " + ex.getMessage());
        }
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void onApplyCoupon() {
        String code = couponField.getText();
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        double subtotal = Session.getCart().stream().mapToDouble(CartLine::getSousTotal).sum();
        try {
            appliedCoupon = couponService.validateCoupon(code.trim(), subtotal);
            currentDiscount = couponService.calculateDiscount(appliedCoupon, subtotal);
            
            couponStatusMsg.setText("Coupon '" + appliedCoupon.getCode() + "' appliqué !");
            couponStatusMsg.setStyle("-fx-text-fill: #16a34a;"); // Vert
            couponStatusMsg.setVisible(true);
            couponStatusMsg.setManaged(true);
            
            updateTotal();
        } catch (Exception e) {
            appliedCoupon = null;
            currentDiscount = 0;
            couponStatusMsg.setText(e.getMessage());
            couponStatusMsg.setStyle("-fx-text-fill: #ef4444;"); // Rouge
            couponStatusMsg.setVisible(true);
            couponStatusMsg.setManaged(true);
            updateTotal();
        }
    }

    private void updateTotal() {
        double subtotal = Session.getCart().stream().mapToDouble(CartLine::getSousTotal).sum();
        
        // Recalculer la réduction si un coupon est déjà appliqué (si le panier a changé)
        if (appliedCoupon != null) {
            try {
                // Re-valider pour vérifier le montant minimum
                couponService.validateCoupon(appliedCoupon.getCode(), subtotal);
                currentDiscount = couponService.calculateDiscount(appliedCoupon, subtotal);
            } catch (Exception e) {
                appliedCoupon = null;
                currentDiscount = 0;
                couponStatusMsg.setText("Coupon retiré : " + e.getMessage());
                couponStatusMsg.setStyle("-fx-text-fill: #ef4444;");
            }
        }

        subtotalLabel.setText(MoneyFormat.amount(subtotal));
        
        if (currentDiscount > 0) {
            discountLabel.setText("-" + MoneyFormat.amount(currentDiscount));
            discountRow.setVisible(true);
            discountRow.setManaged(true);
        } else {
            discountRow.setVisible(false);
            discountRow.setManaged(false);
        }
        
        totalLabel.setText(MoneyFormat.amount(subtotal - currentDiscount));
    }

    @FXML
    private void onRemoveLine() {
        if (Session.isShopManager()) {
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
        if (Session.isShopManager()) {
            shopMessage.setText("Les gestionnaires ne peuvent pas passer commande depuis la boutique.");
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
            double finalTotal = Session.getCart().stream().mapToDouble(CartLine::getSousTotal).sum() - currentDiscount;
            
            // 1. Créer la commande en attente
            long idCommande = orderService.placeOrder(uid, copy, "EN_ATTENTE", finalTotal);
            
            // 2. Enregistrer l'utilisation du coupon si applicable
            if (appliedCoupon != null) {
                couponService.registerUsage(appliedCoupon.getIdCoupon(), uid, idCommande);
            }
            
            // 3. Ouvrir l'interface de paiement Stripe
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shop/PaymentView.fxml"));
            Parent root = loader.load();
            PaymentController controller = loader.getController();
            
            controller.setData(idCommande, finalTotal, () -> {
                // Action à effectuer après le succès du paiement
                Platform.runLater(() -> {
                    Session.getCart().clear();
                    appliedCoupon = null;
                    currentDiscount = 0;
                    couponField.clear();
                    couponStatusMsg.setVisible(false);
                    updateTotal();
                    shopMessage.setText("Paiement réussi ! Votre commande #" + idCommande + " est confirmée.");
                });
            });
            
            Stage stage = new Stage();
            stage.setTitle("Paiement de la commande #" + idCommande);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            
        } catch (Exception e) {
            shopMessage.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
