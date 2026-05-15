package utils;

import entities.Role;
import entities.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;
    private static Role role = Role.ETUDIANT;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    public static User getCurrentUser() {
        return getInstance().currentUser;
    }

    public static void setCurrentUser(User user) {
        getInstance().currentUser = user;
        if (user != null) {
            setRole(user.getRole());
        }
    }

    public void logout() {
        this.currentUser = null;
    }

    public static void setRole(Role r) {
        role = r;
    }

    public static Role getRole() {
        return role;
    }

    public static int getCurrentUserId() {
        SessionManager sm = getInstance();
        return sm.currentUser != null ? sm.currentUser.getIdUtilisateur() : -1;
    }

    public static String getCurrentUserName() {
        SessionManager sm = getInstance();
        return sm.currentUser != null ? sm.currentUser.getNomUtilisateur() : "Inconnu";
    }

    public static Role getCurrentUserRole() {
        SessionManager sm = getInstance();
        return sm.currentUser != null ? sm.currentUser.getRole() : Role.ETUDIANT;
    }

    public static String getCurrentUserRoleLabel() {
        SessionManager sm = getInstance();
        Role r = sm.currentUser != null ? sm.currentUser.getRole() : Role.ETUDIANT;
        return r.name();
    }

    public static boolean isAdmin() {
        SessionManager sm = getInstance();
        return sm.currentUser != null && sm.currentUser.getRole() == Role.ADMIN;
    }

    public static boolean isEnseignant() {
        SessionManager sm = getInstance();
        return sm.currentUser != null && (sm.currentUser.getRole() == Role.ENSEIGNANT || sm.currentUser.getRole() == Role.INSTRUCTEUR);
    }

    public static boolean isEtudiant() {
        SessionManager sm = getInstance();
        return sm.currentUser != null && sm.currentUser.getRole() == Role.ETUDIANT;
    }

    public static boolean isQuizManager() {
        return isAdmin() || isEnseignant();
    }

    public boolean canModify(int ownerUserId) {
        if (currentUser == null) return false;
        if (isAdmin()) return true;
        return currentUser.getIdUtilisateur() == ownerUserId;
    }
}
