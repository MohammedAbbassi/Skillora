package utils;

import entities.Role;
import entities.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setDevUser(Role role) {
        User devUser = new User();
        setValue(devUser, role.name(), "setRole");

        if (role == Role.ADMIN) {
            setValue(devUser, 1001L, "setIdUtilisateur", "setId");
            setValue(devUser, "Admin", "setPrenom");
            setValue(devUser, "Test", "setNom");
            setValue(devUser, "admin_evenements", "setNomUtilisateur");
        } else if (role == Role.INSTRUCTEUR) {
            setValue(devUser, 1002L, "setIdUtilisateur", "setId");
            setValue(devUser, "Enseignant", "setPrenom");
            setValue(devUser, "Test", "setNom");
            setValue(devUser, "enseignant_evenements", "setNomUtilisateur");
        } else {
            setValue(devUser, 1003L, "setIdUtilisateur", "setId");
            setValue(devUser, "Etudiant", "setPrenom");
            setValue(devUser, "Test", "setNom");
            setValue(devUser, "etudiant_evenements", "setNomUtilisateur");
        }

        currentUser = devUser;
    }

    public static User getCurrentUser() {
        User sessionUser = getExternalSessionUser();
        return sessionUser != null ? sessionUser : currentUser;
    }

    public static long getCurrentUserId() {
        User user = getCurrentUser();
        Object id = getValue(user, "getIdUtilisateur", "getId");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return 0;
    }

    public static String getCurrentUserName() {
        User user = getCurrentUser();
        if (user == null) {
            return "Utilisateur non connecte";
        }

        String prenom = toText(getValue(user, "getPrenom"));
        String nom = toText(getValue(user, "getNom"));
        String fullName = (prenom + " " + nom).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        String username = toText(getValue(user, "getNomUtilisateur"));
        if (!username.isEmpty()) {
            return username;
        }

        return "Utilisateur #" + getCurrentUserId();
    }

    public static Role getCurrentUserRole() {
        User user = getCurrentUser();
        Object role = getValue(user, "getRole");
        if (role instanceof Role) {
            return (Role) role;
        }
        Role parsedRole = Role.fromString(toText(role));
        if (parsedRole != null) {
            return parsedRole;
        }
        return getDashboardRoleFallback();
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

    private static User getExternalSessionUser() {
        try {
            Class<?> sessionClass = Class.forName("utils.Session");
            Method getUser = sessionClass.getMethod("getUser");
            Object user = getUser.invoke(null);
            return user instanceof User ? (User) user : null;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static Role getDashboardRoleFallback() {
        try {
            Class<?> sessionClass = Class.forName("controllers.SessionManager");
            Method getRole = sessionClass.getMethod("getRole");
            Object dashboardRole = getRole.invoke(null);
            String value = toText(dashboardRole);
            if ("ADMIN".equalsIgnoreCase(value)) {
                return Role.ADMIN;
            }
            if ("INSTRUCTOR".equalsIgnoreCase(value) || "INSTRUCTEUR".equalsIgnoreCase(value)) {
                return Role.INSTRUCTEUR;
            }
            if ("USER".equalsIgnoreCase(value)) {
                return Role.ETUDIANT;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return null;
    }

    private static Object getValue(Object target, String... methodNames) {
        if (target == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Try the next compatible getter.
            }
        }
        return null;
    }

    private static void setValue(Object target, Object value, String... methodNames) {
        if (target == null) {
            return;
        }

        for (String methodName : methodNames) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                try {
                    Object compatibleValue = convertValue(value, method.getParameterTypes()[0]);
                    method.invoke(target, compatibleValue);
                    return;
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                    // Try the next compatible setter.
                }
            }
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if ((targetType == int.class || targetType == Integer.class) && value instanceof Number) {
            return ((Number) value).intValue();
        }
        if ((targetType == long.class || targetType == Long.class) && value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        if (targetType == Role.class) {
            return Role.fromString(String.valueOf(value));
        }
        return value;
    }

    private static String toText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
