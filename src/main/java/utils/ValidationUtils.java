package utils;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isBlank(String value) {
        return trimToEmpty(value).isEmpty();
    }

    public static boolean hasMinLength(String value, int minChars) {
        return trimToEmpty(value).length() >= minChars;
    }
}

