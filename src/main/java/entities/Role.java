package entities;

public enum Role {
    ETUDIANT,
<<<<<<< HEAD
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
=======
    ENSEIGNANT,
<<<<<<< Updated upstream
=======
    INSTRUCTEUR,
>>>>>>> Stashed changes
    ADMIN
}
>>>>>>> amine
