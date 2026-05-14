package com.skillora.shop.controllers;

import com.skillora.shop.entities.CategorieProduit;
import com.skillora.shop.entities.Produit;
import com.skillora.shop.services.ProduitCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProductFormDialogController {

    @FXML private Label dialogTitle;
    @FXML private TextField fieldNom;
    @FXML private TextArea fieldDesc;
    @FXML private TextField fieldPrix;
    @FXML private TextField fieldLangue;
    @FXML private ComboBox<CategorieProduit> comboCategorie;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private Label errorMsg;
    @FXML private Button btnConfirm;
    @FXML private ImageView imagePreview;
    @FXML private Label imagePlaceholder;
    @FXML private TextField fieldImagePath;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private Produit existingProduct;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        comboCategorie.setItems(FXCollections.observableArrayList(CategorieProduit.values()));
        comboNiveau.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        
        // Defaults
        comboCategorie.getSelectionModel().select(CategorieProduit.FORMATION);
        comboNiveau.getSelectionModel().selectFirst();
    }

    public void setProduct(Produit p) {
        this.existingProduct = p;
        if (p != null) {
            dialogTitle.setText("Modifier le produit");
            fieldNom.setText(p.getNom());
            fieldDesc.setText(p.getDescription());
            fieldPrix.setText(String.format("%.2f", p.getPrix()));
            fieldLangue.setText(p.getLangue());
            comboCategorie.getSelectionModel().select(p.getCategorie() != null ? p.getCategorie() : CategorieProduit.FORMATION);
            comboNiveau.getSelectionModel().select(p.getNiveau());
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                updateImagePreview(p.getImage());
            }
            btnConfirm.setText("Mettre à jour");
        } else {
            dialogTitle.setText("Ajouter un nouveau produit");
            btnConfirm.setText("Ajouter");
        }
    }

    private void updateImagePreview(String path) {
        if (path == null || path.isEmpty()) {
            imagePreview.setImage(null);
            imagePlaceholder.setVisible(true);
            return;
        }

        try {
            // Si c'est une URL de l'ancien format pollinations.ai/p/, on la convertit vers le format direct
            if (path.contains("pollinations.ai/p/")) {
                path = path.replace("pollinations.ai/p/", "image.pollinations.ai/prompt/");
            }

            Image img;
            if (path.startsWith("http")) {
                // Pour les URLs, on utilise le chargement asynchrone pour ne pas bloquer l'UI
                img = new Image(path, true);
                
                imagePlaceholder.setVisible(true); // Afficher le placeholder pendant le chargement
                adminMsg("Chargement de l'image...");
                
                img.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV.doubleValue() >= 1.0 && !img.isError()) {
                        javafx.application.Platform.runLater(() -> {
                            imagePlaceholder.setVisible(false);
                            imagePreview.setImage(img);
                            adminMsg("Image chargée !");
                        });
                    }
                });

                img.errorProperty().addListener((obs, oldV, newV) -> {
                    if (newV) {
                        javafx.application.Platform.runLater(() -> {
                            showError("Erreur de chargement. L'URL est peut-être invalide.");
                            imagePlaceholder.setVisible(true);
                            imagePreview.setImage(null);
                        });
                    }
                });
                
                // On attache l'image même si elle charge, JavaFX l'affichera au fur et à mesure
                imagePreview.setImage(img);
            } else {
                File file = new File(path);
                if (file.exists()) {
                    img = new Image(file.toURI().toString());
                    imagePreview.setImage(img);
                    imagePlaceholder.setVisible(false);
                } else {
                    throw new Exception("Fichier introuvable");
                }
            }
            fieldImagePath.setText(path);
        } catch (Exception e) {
            imagePlaceholder.setVisible(true);
            imagePreview.setImage(null);
            showError("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void onBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(fieldNom.getScene().getWindow());
        if (file != null) {
            updateImagePreview(file.getAbsolutePath());
        }
    }

    @FXML
    private void onGenerateAIImage() {
        String name = fieldNom.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Veuillez saisir au moins le nom du produit pour générer une image.");
            return;
        }
        
        errorMsg.setVisible(false);
        try {
            // Utilisation de l'API directe d'image de pollinations.ai
            String prompt = name + " " + (fieldDesc.getText() != null ? fieldDesc.getText() : "");
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString());
            
            // L'URL correcte pour obtenir directement l'image est https://image.pollinations.ai/prompt/
            String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=512&height=512&seed=" + System.currentTimeMillis() + "&nologo=true";
            
            updateImagePreview(imageUrl);
            adminMsg("Image générée par l'IA !");
        } catch (Exception e) {
            showError("Erreur lors de la génération de l'image: " + e.getMessage());
        }
    }

    private void adminMsg(String msg) {
        errorMsg.setText(msg);
        errorMsg.setStyle("-fx-text-fill: #22c55e;"); // Green for success
        errorMsg.setVisible(true);
        errorMsg.setManaged(true);
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void onConfirm() {
        try {
            validateAndSave();
            if (onSuccess != null) {
                onSuccess.run();
            }
            close();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Une erreur est survenue: " + e.getMessage());
        }
    }

    private void validateAndSave() throws Exception {
        String nom = fieldNom.getText() == null ? "" : fieldNom.getText().trim();
        if (nom.isEmpty()) throw new IllegalArgumentException("Le nom est obligatoire.");

        String prixStr = fieldPrix.getText() == null ? "" : fieldPrix.getText().trim().replace(",", ".");
        if (prixStr.isEmpty()) throw new IllegalArgumentException("Le prix est obligatoire.");
        
        double prix;
        try {
            prix = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix doit être un nombre valide.");
        }
        
        if (prix <= 0) {
            throw new IllegalArgumentException("Le prix doit être un nombre positif.");
        }

        Produit p = (existingProduct != null) ? existingProduct : new Produit();
        p.setNom(nom);
        p.setDescription(fieldDesc.getText() == null ? "" : fieldDesc.getText().trim());
        p.setPrix(prix);
        p.setLangue(fieldLangue.getText() == null ? "" : fieldLangue.getText().trim());
        p.setCategorie(comboCategorie.getValue());
        p.setNiveau(comboNiveau.getValue());
        p.setImage(fieldImagePath.getText() != null ? fieldImagePath.getText().trim() : null);

        if (existingProduct != null) {
            produitCRUD.modifier(p);
        } else {
            produitCRUD.ajouter(p);
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void showError(String msg) {
        errorMsg.setText(msg);
        errorMsg.setVisible(true);
        errorMsg.setManaged(true);
    }

    private void close() {
        Stage stage = (Stage) fieldNom.getScene().getWindow();
        stage.close();
    }
}
