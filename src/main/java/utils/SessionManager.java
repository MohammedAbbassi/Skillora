package utils;

import entities.Role;
import entities.User;

/**
 * Singleton that holds the currently logged-in user for the whole session.
 * Set it once at login, read it anywhere.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    // ── Convenience helpers ────────────────────────────────────

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getIdUtilisateur() : -1;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNomUtilisateur() : "Inconnu";
    }

    public Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isEnseignant() {
        return currentUser != null && currentUser.getRole() == Role.ENSEIGNANT;
    }

    public boolean isEtudiant() {
        return currentUser != null && currentUser.getRole() == Role.ETUDIANT;
    }

    /**
     * Returns true if the current user can edit/delete the resource
     * owned by ownerUserId.
     * - ADMIN  : always yes
     * - Others : only if they own it
     */
    public boolean canModify(int ownerUserId) {
        if (currentUser == null) return false;
        if (isAdmin()) return true;
        return currentUser.getIdUtilisateur() == ownerUserId;
    }
}
