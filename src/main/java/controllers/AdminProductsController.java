package controllers;

import entities.CategorieProduit;
import entities.Produit;
import services.ProduitCRUD;
import utils.DialogHelper;
import utils.MoneyFormat;
import utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminProductsController {

    @FXML
    private TableView<Produit> productTable;
    @FXML
    private TableColumn<Produit, String> colId;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, String> colCategorie;
    @FXML
    private TableColumn<Produit, String> colLangue;
    @FXML
    private TableColumn<Produit, String> colPrix;
    @FXML
    private TableColumn<Produit, String> colNiveau;
    @FXML
    private TextField fieldNom;
    @FXML
    private TextArea fieldDesc;
    @FXML
    private TextField fieldPrix;
    @FXML
    private TextField fieldLangue;
    @FXML
    private ComboBox<CategorieProduit> comboCategorie;
    @FXML
    private ComboBox<String> comboNiveau;
    @FXML
    private Label adminMsg;

    private final ProduitCRUD produitCRUD = new ProduitCRUD();

    @FXML
    private void initialize() {
        if (!Session.isAdmin()) {
            adminMsg.setText("Accès réservé à l’administrateur.");
            return;
        }
        comboCategorie.setItems(FXCollections.observableArrayList(CategorieProduit.values()));
        comboCategorie.getSelectionModel().select(CategorieProduit.FORMATION);
        comboNiveau.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        comboNiveau.getSelectionModel().selectFirst();

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colCategorie.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategorie() == null ? "" : c.getValue().getCategorie().name()));
        colLangue.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getLangue())));
        colPrix.setCellValueFactory(c -> new SimpleStringProperty(MoneyFormat.amount(c.getValue().getPrix())));
        colNiveau.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getNiveau())));
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        productTable.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                fieldNom.setText(p.getNom());
                fieldDesc.setText(str(p.getDescription()));
                fieldPrix.setText(String.format("%.2f", p.getPrix()));
                fieldLangue.setText(str(p.getLangue()));
                comboCategorie.getSelectionModel().select(
                        p.getCategorie() != null ? p.getCategorie() : CategorieProduit.FORMATION);
                comboNiveau.getSelectionModel().select(p.getNiveau() != null ? p.getNiveau() : "DEBUTANT");
            }
        });

        reload();
    }

    public void reload() {
        adminMsg.setText("");
        if (!Session.isAdmin()) {
            return;
        }
        try {
            productTable.setItems(FXCollections.observableArrayList(produitCRUD.getAll()));
        } catch (Exception e) {
            adminMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        if (!Session.isAdmin()) {
            return;
        }
        adminMsg.setText("");
        try {
            Produit p = buildFromForm(null);
            produitCRUD.add(p);
            clearForm();
            reload();
            adminMsg.setText("Produit ajouté.");
        } catch (Exception e) {
            adminMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        Produit sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            adminMsg.setText("Sélectionnez un produit dans le tableau.");
            return;
        }
        adminMsg.setText("");
        try {
            Produit p = buildFromForm(sel.getId());
            produitCRUD.update(p);
            reload();
            adminMsg.setText("Produit mis à jour.");
        } catch (Exception e) {
            adminMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Produit sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            adminMsg.setText("Sélectionnez un produit.");
            return;
        }
        boolean ok = DialogHelper.showConfirm(
            adminMsg.getScene().getWindow(),
            "Supprimer ce produit ?",
            sel.getNom());
        if (!ok) return;
        adminMsg.setText("");
        try {
            produitCRUD.delete(sel);
            clearForm();
            reload();
            adminMsg.setText("Produit supprimé.");
        } catch (Exception e) {
            adminMsg.setText(e.getMessage());
        }
    }

    @FXML
    private void onClearForm() {
        clearForm();
        productTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        fieldNom.clear();
        fieldDesc.clear();
        fieldPrix.clear();
        fieldLangue.clear();
        comboCategorie.getSelectionModel().select(CategorieProduit.FORMATION);
        comboNiveau.getSelectionModel().selectFirst();
    }

    private Produit buildFromForm(Long id) {
        String nom = trim(fieldNom.getText());
        if (nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        double prix = Double.parseDouble(trim(fieldPrix.getText()).replace(",", "."));
        Produit p = new Produit();
        if (id != null) {
            p.setId(id);
        }
        p.setNom(nom);
        p.setDescription(trim(fieldDesc.getText()));
        p.setPrix(prix);
        p.setLangue(trim(fieldLangue.getText()));
        p.setCategorie(comboCategorie.getSelectionModel().getSelectedItem());
        p.setNiveau(comboNiveau.getSelectionModel().getSelectedItem());
        return p;
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }
}
