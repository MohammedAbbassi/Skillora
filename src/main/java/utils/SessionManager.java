package utils;

import entities.Role;
import entities.User;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static long getCurrentUserId() {
        return currentUser != null ? currentUser.getIdUtilisateur() : 0;
    }

    public static String getCurrentUserName() {
        if (currentUser == null) {
            return "Utilisateur non connecte";
        }

        String prenom = currentUser.getPrenom() != null ? currentUser.getPrenom().trim() : "";
        String nom = currentUser.getNom() != null ? currentUser.getNom().trim() : "";
        String fullName = (prenom + " " + nom).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (currentUser.getNomUtilisateur() != null && !currentUser.getNomUtilisateur().trim().isEmpty()) {
            return currentUser.getNomUtilisateur().trim();
        }

        return "Utilisateur #" + currentUser.getIdUtilisateur();
    }

    public static Role getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static String getCurrentUserRoleLabel() {
        Role role = getCurrentUserRole();
        if (role == Role.ADMIN) {
            return "Admin";
        }
        if (role == Role.INSTRUCTEUR) {
            return "Enseignant";
        }
        if (role == Role.ETUDIANT) {
            return "Etudiant";
        }
        return "Role inconnu";
    }
}
