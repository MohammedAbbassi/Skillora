package entities;

public enum Role {
    ETUDIANT,
    INSTRUCTEUR,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        if ("ENSEIGNANT".equalsIgnoreCase(roleStr.trim())) {
            return INSTRUCTEUR;
        }
        try {
            return Role.valueOf(roleStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown role: " + roleStr);
            return null;
        }
    }
}
