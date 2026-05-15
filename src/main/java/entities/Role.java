package entities;

public enum Role {
    ETUDIANT,
    ENSEIGNANT,
    INSTRUCTEUR,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        try {
            return Role.valueOf(roleStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown role: " + roleStr);
            return null;
        }
    }
}
