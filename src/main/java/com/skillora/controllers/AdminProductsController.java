package com.skillora.controllers;

import Entities.CategorieProduit;
import Entities.Produit;
import Services.ProduitCRUD;
import com.skillora.MoneyFormat;
import com.skillora.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.File;

public class AdminProductsController {

    @FXML
    private ListView<Produit> productList;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private Label adminMsg;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();

    @FXML
    private void initialize() {
        if (!Session.isAdmin()) {
            adminMsg.setText("Accès réservé à l’administrateur.");
            return;
        }

        productList.setCellFactory(lv -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox mainBox = new HBox(15);
                    mainBox.setAlignment(Pos.CENTER_LEFT);
                    mainBox.setPadding(new Insets(10));
                    mainBox.setStyle("-fx-background-color: transparent; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                    // Image du produit
                    StackPane imgContainer = new StackPane();
                    imgContainer.setPrefSize(60, 60);
                    imgContainer.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");
                    
                    ImageView iv = new ImageView();
                    iv.setFitWidth(60);
                    iv.setFitHeight(60);
                    iv.setPreserveRatio(true);
                    
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
                        } catch (Exception e) {
                            // Ignorer les erreurs d'image
                        }
                    }
                    imgContainer.getChildren().add(iv);

                    VBox infoBox = new VBox(5);
                    HBox.setHgrow(infoBox, Priority.ALWAYS);
                    
                    Label nomLbl = new Label(p.getNom());
                    nomLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
                    
                    String cat = p.getCategorie() == null ? "N/A" : p.getCategorie().name();
                    Label detailsLbl = new Label(String.format("Catégorie: %s  •  Langue: %s  •  Niveau: %s", cat, str(p.getLangue()), str(p.getNiveau())));
                    detailsLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                    
                    infoBox.getChildren().addAll(nomLbl, detailsLbl);

                    Label prixLbl = new Label(MoneyFormat.amount(p.getPrix()));
                    prixLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 16px; -fx-min-width: 80px; -fx-alignment: center-right;");

                    mainBox.getChildren().addAll(imgContainer, infoBox, prixLbl);

                    setText(null);
                    setGraphic(mainBox);
                }
            }
        });

        productList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterProducts(newVal);
        });

        reload();
    }

    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            reload();
            return;
        }
        String lowerQuery = query.toLowerCase().trim();
        try {
            productList.setItems(FXCollections.observableArrayList(
                produitCRUD.afficher().stream()
                    .filter(p -> p.getNom().toLowerCase().contains(lowerQuery) || 
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery)))
                    .toList()
            ));
        } catch (Exception e) {
            adminMsg.setText("Erreur lors du filtrage: " + e.getMessage());
        }
    }

    public void reload() {
        adminMsg.setText("");
        if (!Session.isAdmin()) {
            return;
        }
        try {
            productList.setItems(FXCollections.observableArrayList(produitCRUD.afficher()));
        } catch (Exception e) {
            adminMsg.setText("Erreur lors du chargement: " + e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        showProductDialog(null);
    }

    @FXML
    private void onEdit() {
        Produit selected = productList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        }
    }

    private void showProductDialog(Produit p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shop/ProductFormDialog.fxml"));
            Parent root = loader.load();
            
            ProductFormDialogController controller = loader.getController();
            controller.setProduct(p);
            controller.setOnSuccess(this::reload);

            Stage stage = new Stage();
            stage.setTitle(p == null ? "Ajouter un produit" : "Modifier le produit");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            adminMsg.setText("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        Produit sel = productList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            adminMsg.setText("Sélectionnez un produit à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer ce produit ?");
        confirm.setContentText(sel.getNom());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        adminMsg.setText("");
        try {
            produitCRUD.supprimer(sel.getId().intValue());
            reload();
            adminMsg.setText("Produit supprimé.");
        } catch (Exception e) {
            adminMsg.setText("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }
}
