package controllers;

public final class SessionManager {

    public enum Role {
        ADMIN, INSTRUCTOR, USER
    }

    private static Role role = readRole();

    private SessionManager() {
    }

    public static Role getRole() {
        return role;
    }

    public static void setRole(Role role) {
        SessionManager.role = role == null ? Role.USER : role;
    }

    public static boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public static boolean isQuizManager() {
        return role == Role.ADMIN || role == Role.INSTRUCTOR;
    }

    private static Role readRole() {
        String configuredRole = System.getProperty("skillora.role");
        if (configuredRole == null || configuredRole.isBlank()) {
            configuredRole = System.getenv("SKILLORA_ROLE");
        }

        if ("USER".equalsIgnoreCase(configuredRole)) {
            return Role.USER;
        }
        if ("INSTRUCTOR".equalsIgnoreCase(configuredRole) || "INSTRUCTEUR".equalsIgnoreCase(configuredRole)) {
            return Role.INSTRUCTOR;
        }
        return Role.ADMIN;
    }
}
