package com.skillora.shop;

import com.skillora.shop.entities.Produit;
import com.skillora.shop.entities.Role;
import com.skillora.shop.entities.User;
import com.skillora.shop.model.CartLine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Session {

    private static User currentUser;
    private static final ObservableList<CartLine> cart = FXCollections.observableArrayList();

    private Session() {
    }

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
        cart.clear();
    }

    public static ObservableList<CartLine> getCart() {
        return cart;
    }

    public static void addOrMergeToCart(Produit p, int quantite) {
        if (isShopManager()) {
            return;
        }
        if (quantite <= 0) {
            return;
        }
        for (CartLine line : cart) {
            if (line.getIdProduit() == p.getId()) {
                line.setQuantite(line.getQuantite() + quantite);
                return;
            }
        }
        cart.add(CartLine.fromProduit(p, quantite));
    }

    public static void removeLine(CartLine line) {
        cart.remove(line);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public static boolean isInstructor() {
        return currentUser != null && currentUser.getRole() == Role.INSTRUCTEUR;
    }

    public static boolean isShopManager() {
        return isAdmin() || isInstructor();
    }
}
